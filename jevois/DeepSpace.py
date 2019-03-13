import libjevois as jevois
import cv2
import numpy as np
import math
import json


def valid_contour(contour):
    return cv2.contourArea(contour) > 25


# Detects hatches in FRC 2019 game Destination: Deep Space presented by the Boeing Company
#
# @videomapping YUYV 320 240 30 YUYV 320 240 30 TeamMeanMachine DeepSpace
# @ingroup modules
# noinspection PyUnresolvedReferences
def stretch_rect(rect, factor):
    center, size, angle = rect

    if size[0] > size[1]:
        size = (size[0] * factor, size[1])
    else:
        size = (size[0], size[1] * factor)

    return center, size, angle


def dist_points(a, b):
    return math.hypot(b[0] - a[0], b[1] - a[1])


class DeepSpace:
    def __init__(self):
        # Instantiate a JeVois Timer to measure our processing framerate:
        self.timer = jevois.Timer("processing timer", 100, jevois.LOG_INFO)
        self.profiler = jevois.Profiler("Profiler", 100, jevois.LOG_INFO)

        self.active = False
        self.w = None
        self.h = None
        self.fov = 90.0  # degrees
        self.tape_diag = math.hypot(2.0, 5.5)  # inches
        self.tape_fw = 349.96
        self.target_w = 11.3115  # inches
        self.hsv_min = (60, 40, 40)
        self.hsv_max = (90, 255, 255)
        self.open_kernel = np.ones((7, 7), np.uint8)
        self.stretch_factor = 16.0


    def processNoUSB(self, inframe):
        process(inframe)


    ####################################################################################################
    # Process function with USB output
    def process(self, inframe, outframe=None):
        # Get the next camera image (may block until it is captured) and here convert it to OpenCV BGR. If you need a
        # grayscale image, just use getCvGRAY() instead of getCvBGR(). Also supported are getCvRGB() and getCvRGBA():
        inimg = inframe.getCvBGR()

        h, w, _ = inimg.shape

        self.w = w
        self.h = h
        # Start measuring image processing time (NOTE: does not account for input conversion time):
        self.timer.start()

        if self.active:
            self.profiler.start()

            hsv = cv2.cvtColor(inimg, cv2.COLOR_BGR2HSV)
            threshold = cv2.inRange(hsv, self.hsv_min, self.hsv_max)
            self.profiler.checkpoint("HSV Thresholding")

            opened = cv2.morphologyEx(threshold, cv2.MORPH_OPEN, self.open_kernel)
            self.profiler.checkpoint("Morph open")

            contours, _ = cv2.findContours(opened, cv2.RETR_LIST, cv2.CHAIN_APPROX_SIMPLE)
            self.profiler.checkpoint("Find contours")

            rects = [cv2.minAreaRect(c) for c in contours if valid_contour(c)]

            rects.sort(key=lambda r: r[0][0])
            self.profiler.checkpoint("Find rects")

            pairs = self.group_rects(rects)
            self.profiler.checkpoint("Pair rects")

            targets = []
            for left, right in pairs:
                l_center, l_size, l_angle = left
                r_center, r_size, r_angle = right

                ldist = self.tape_diag * self.tape_fw / math.hypot(*l_size)
                rdist = self.tape_diag * self.tape_fw / math.hypot(*r_size)
                skew_angle = math.degrees(math.asin((rdist - ldist) / self.target_w))
                act_dist = (ldist + rdist) / 2.0
                center_x = (l_center[0] + r_center[0]) / 2.0
                angle = (center_x / self.w - 0.5) * self.fov

                targets.append({
                    'distance': act_dist,
                    'angle': angle,
                    'skew': skew_angle
                })

            jevois.sendSerial(json.dumps(targets))
            outimg = inimg
        else:
            outimg = cv2.GaussianBlur(inimg, (5, 5), 0)

        fps = self.timer.stop()
        if outframe is not None:
            # Drawing stuff
            outimg = inimg


            if self.active:
                cv2.drawContours(outimg, contours, -1, (255, 0, 0), 3)
                cv2.drawContours(outimg, [np.int0(cv2.boxPoints(r)) for r in rects], -1, (0, 0, 255), 2)

                for (left, right) in pairs:
                    cv2.line(outimg, (int(left[0][0]), int(left[0][1])),
                             (int(right[0][0]), int(right[0][1])), (0, 255, 0), 2)

                    ldist = self.tape_diag * self.tape_fw / math.hypot(*left[1])
                    rdist = self.tape_diag * self.tape_fw / math.hypot(*right[1])
                    skew_angle = math.degrees(math.asin((rdist - ldist) / self.target_w))

                    x = (left[0][0] + right[0][0]) / 2.0
                    y = (left[0][1] + right[0][1]) / 2.0 + 35
                    cv2.putText(outimg, "{0:.3f}, {1:.3f}, {2:.3f}".format(ldist, rdist, skew_angle), (int(x), int(y)),
                                cv2.FONT_HERSHEY_SIMPLEX,
                                0.5, (255, 255, 255))

            # Write frames/s info from our timer into the edge map (NOTE: does not account for output conversion time):
            height = outimg.shape[0]
            cv2.putText(outimg, fps, (3, height - 6), cv2.FONT_HERSHEY_SIMPLEX, 0.3, (255, 255, 255))
            outframe.sendCv(outimg)
            self.profiler.checkpoint("Drawing")

        self.profiler.stop()

        # Convert our output image to video output format and send to host over USB:

    def group_rects(self, rects):
        pairs = []
        prev_rect = None
        prev_stretched = None
        for rect in rects:
            stretched = stretch_rect(rect, self.stretch_factor)

            if prev_rect is None:
                prev_rect = rect
                prev_stretched = stretched
                continue

            intersect, region = cv2.rotatedRectangleIntersection(prev_stretched, stretched)
            if region is None:
                prev_rect = rect
                prev_stretched = stretched
                continue

            base_y = (prev_rect[0][1] + rect[0][1]) / 2.0
            if region[0][0][1] < base_y:
                pairs.append((prev_rect, rect))
                prev_rect = None
                prev_stretched = None
            else:
                prev_rect = rect
                prev_stretched = stretched

        return pairs
