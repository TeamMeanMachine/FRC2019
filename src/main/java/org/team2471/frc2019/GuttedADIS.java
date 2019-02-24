package org.team2471.frc2019;

import edu.wpi.first.hal.FRCNetComm.tResourceType;
import edu.wpi.first.hal.HAL;
import edu.wpi.first.wpilibj.*;
import edu.wpi.first.wpilibj.interfaces.Gyro;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class is for the ADIS16448 IMU that connects to the RoboRIO MXP port.
 */
@SuppressWarnings("unused")
public class GuttedADIS extends GyroBase implements Gyro, PIDSource, Sendable {
    private static final double kCalibrationSampleTime = 5.0; // Calibration time in seconds
    private static final double kDegreePerSecondPerLSB = 1.0 / 25.0;
    private static final double kGPerLSB = 1.0 / 1200.0;
    private static final double kMilligaussPerLSB = 1.0 / 7.0;
    private static final double kMillibarPerLSB = 0.02;
    private static final double kDegCPerLSB = 0.07386;
    private static final double kDegCOffset = 31;

    private static final int kGLOB_CMD = 0x3E;
    private static final int kRegSMPL_PRD = 0x36;
    private static final int kRegSENS_AVG = 0x38;
    private static final int kRegMSC_CTRL = 0x34;
    private static final int kRegPROD_ID = 0x56;
    private static final int kRegXGYRO_OFF = 0x1A;

    public enum Axis {kX, kY, kZ}

    // AHRS yaw axis
    private Axis m_yaw_axis;

    //CRC-16 Look-Up Table
    int adiscrc[] = new int[]{
            0x0000, 0x17CE, 0x0FDF, 0x1811, 0x1FBE, 0x0870, 0x1061, 0x07AF,
            0x1F3F, 0x08F1, 0x10E0, 0x072E, 0x0081, 0x174F, 0x0F5E, 0x1890,
            0x1E3D, 0x09F3, 0x11E2, 0x062C, 0x0183, 0x164D, 0x0E5C, 0x1992,
            0x0102, 0x16CC, 0x0EDD, 0x1913, 0x1EBC, 0x0972, 0x1163, 0x06AD,
            0x1C39, 0x0BF7, 0x13E6, 0x0428, 0x0387, 0x1449, 0x0C58, 0x1B96,
            0x0306, 0x14C8, 0x0CD9, 0x1B17, 0x1CB8, 0x0B76, 0x1367, 0x04A9,
            0x0204, 0x15CA, 0x0DDB, 0x1A15, 0x1DBA, 0x0A74, 0x1265, 0x05AB,
            0x1D3B, 0x0AF5, 0x12E4, 0x052A, 0x0285, 0x154B, 0x0D5A, 0x1A94,
            0x1831, 0x0FFF, 0x17EE, 0x0020, 0x078F, 0x1041, 0x0850, 0x1F9E,
            0x070E, 0x10C0, 0x08D1, 0x1F1F, 0x18B0, 0x0F7E, 0x176F, 0x00A1,
            0x060C, 0x11C2, 0x09D3, 0x1E1D, 0x19B2, 0x0E7C, 0x166D, 0x01A3,
            0x1933, 0x0EFD, 0x16EC, 0x0122, 0x068D, 0x1143, 0x0952, 0x1E9C,
            0x0408, 0x13C6, 0x0BD7, 0x1C19, 0x1BB6, 0x0C78, 0x1469, 0x03A7,
            0x1B37, 0x0CF9, 0x14E8, 0x0326, 0x0489, 0x1347, 0x0B56, 0x1C98,
            0x1A35, 0x0DFB, 0x15EA, 0x0224, 0x058B, 0x1245, 0x0A54, 0x1D9A,
            0x050A, 0x12C4, 0x0AD5, 0x1D1B, 0x1AB4, 0x0D7A, 0x156B, 0x02A5,
            0x1021, 0x07EF, 0x1FFE, 0x0830, 0x0F9F, 0x1851, 0x0040, 0x178E,
            0x0F1E, 0x18D0, 0x00C1, 0x170F, 0x10A0, 0x076E, 0x1F7F, 0x08B1,
            0x0E1C, 0x19D2, 0x01C3, 0x160D, 0x11A2, 0x066C, 0x1E7D, 0x09B3,
            0x1123, 0x06ED, 0x1EFC, 0x0932, 0x0E9D, 0x1953, 0x0142, 0x168C,
            0x0C18, 0x1BD6, 0x03C7, 0x1409, 0x13A6, 0x0468, 0x1C79, 0x0BB7,
            0x1327, 0x04E9, 0x1CF8, 0x0B36, 0x0C99, 0x1B57, 0x0346, 0x1488,
            0x1225, 0x05EB, 0x1DFA, 0x0A34, 0x0D9B, 0x1A55, 0x0244, 0x158A,
            0x0D1A, 0x1AD4, 0x02C5, 0x150B, 0x12A4, 0x056A, 0x1D7B, 0x0AB5,
            0x0810, 0x1FDE, 0x07CF, 0x1001, 0x17AE, 0x0060, 0x1871, 0x0FBF,
            0x172F, 0x00E1, 0x18F0, 0x0F3E, 0x0891, 0x1F5F, 0x074E, 0x1080,
            0x162D, 0x01E3, 0x19F2, 0x0E3C, 0x0993, 0x1E5D, 0x064C, 0x1182,
            0x0912, 0x1EDC, 0x06CD, 0x1103, 0x16AC, 0x0162, 0x1973, 0x0EBD,
            0x1429, 0x03E7, 0x1BF6, 0x0C38, 0x0B97, 0x1C59, 0x0448, 0x1386,
            0x0B16, 0x1CD8, 0x04C9, 0x1307, 0x14A8, 0x0366, 0x1B77, 0x0CB9,
            0x0A14, 0x1DDA, 0x05CB, 0x1205, 0x15AA, 0x0264, 0x1A75, 0x0DBB,
            0x152B, 0x02E5, 0x1AF4, 0x0D3A, 0x0A95, 0x1D5B, 0x054A, 0x1284
    };

    // gyro offset
    private double m_gyro_offset_x = 0.0;
    private double m_gyro_offset_y = 0.0;
    private double m_gyro_offset_z = 0.0;

    // last read values (post-scaling)
    private double m_gyro_x = 0.0;
    private double m_gyro_y = 0.0;
    private double m_gyro_z = 0.0;

    // accumulated gyro values (for offset calculation)
    private int m_accum_count = 0;
    private double m_accum_gyro_x = 0.0;
    private double m_accum_gyro_y = 0.0;
    private double m_accum_gyro_z = 0.0;

    // integrated gyro values
    private double m_integ_gyro_x = 0.0;
    private double m_integ_gyro_y = 0.0;
    private double m_integ_gyro_z = 0.0;

    // last sample time
    private double m_last_sample_time = 0.0;

    private AtomicBoolean m_freed = new AtomicBoolean(false);

    private SPI m_spi;
    private DigitalInput m_interrupt;

    // Sample from the IMU
    private static class Sample {
        public double gyro_x;
        public double gyro_y;
        public double gyro_z;
        public double accel_x;
        public double accel_y;
        public double accel_z;
        public double mag_x;
        public double mag_y;
        public double mag_z;
        public double baro;
        public double temp;
        public double dt;

        // Swap axis as appropriate for yaw axis selection
        public void adjustYawAxis(Axis yaw_axis) {
            switch (yaw_axis) {
                case kX: {
                    // swap X and Z
                    double tmp;
                    tmp = accel_x;
                    accel_x = accel_z;
                    accel_z = tmp;
                    tmp = mag_x;
                    mag_x = mag_z;
                    mag_z = tmp;
                    tmp = gyro_x;
                    gyro_x = gyro_z;
                    gyro_z = tmp;
                    break;
                }
                case kY: {
                    // swap Y and Z
                    double tmp;
                    tmp = accel_y;
                    accel_y = accel_z;
                    accel_z = tmp;
                    tmp = mag_y;
                    mag_y = mag_z;
                    mag_z = tmp;
                    tmp = gyro_y;
                    gyro_y = gyro_z;
                    gyro_z = tmp;
                    break;
                }
                case kZ:
                default:
                    // no swap required
                    break;
            }
        }
    }

    // Sample FIFO
    private static final int kSamplesDepth = 10;
    private final Sample[] m_samples;
    private final Lock m_samples_mutex;
    private final Condition m_samples_not_empty;
    private int m_samples_count = 0;
    private int m_samples_take_index = 0;
    private int m_samples_put_index = 0;
    private boolean m_calculate_started = false;

    // Previous timestamp
    long timestamp_old = 0;

    private static class AcquireTask implements Runnable {
        private GuttedADIS imu;

        public AcquireTask(GuttedADIS imu) {
            this.imu = imu;
        }

        @Override
        public void run() {
            imu.acquire();
        }
    }

    private static class CalculateTask implements Runnable {
        private GuttedADIS imu;

        public CalculateTask(GuttedADIS imu) {
            this.imu = imu;
        }

        @Override
        public void run() {
            imu.calculate();
        }
    }

    private Thread m_acquire_task;
    private Thread m_calculate_task;

    /**
     * @param yaw_axis Which axis is Yaw
     */
    public GuttedADIS(Axis yaw_axis) {
        m_yaw_axis = yaw_axis;

        // Force the IMU reset pin to toggle on startup (doesn't require DS enable)
        DigitalOutput m_reset_out = new DigitalOutput(18);  // Drive MXP DIO8 low
        Timer.delay(0.01);  // Wait 10ms
        m_reset_out.close();
        DigitalInput m_reset_in = new DigitalInput(18);  // Set MXP DIO8 high
        Timer.delay(0.5);  // Wait 500ms

        m_spi = new SPI(SPI.Port.kMXP);
        m_spi.setClockRate(1000000);
        m_spi.setMSBFirst();
        m_spi.setSampleDataOnFalling();
        m_spi.setClockActiveLow();
        m_spi.setChipSelectActiveLow();

        readRegister(kRegPROD_ID); // dummy read

        // Validate the product ID
        if (readRegister(kRegPROD_ID) != 16448) {
            m_spi.free();
            m_spi = null;
            m_samples = null;
            m_samples_mutex = null;
            m_samples_not_empty = null;
            DriverStation.reportError("could not find ADIS16448", false);
            return;
        }

        // Set IMU internal decimation to 102.4 SPS
        writeRegister(kRegSMPL_PRD, 0x0301);

        // Enable Data Ready (LOW = Good Data) on DIO1 (PWM0 on MXP) & PoP
        writeRegister(kRegMSC_CTRL, 0x0056);

        // Configure IMU internal Bartlett filter
        writeRegister(kRegSENS_AVG, 0x0402);

        // Read serial number and lot ID
        //m_serial_num = readRegister(kRegSERIAL_NUM);
        //m_lot_id2 = readRegister(kRegLOT_ID2);
        //m_lot_id1 = readRegister(kRegLOT_ID1);

        // Create data acq FIFO.  We make the FIFO 2 longer than it needs
        // to be so the input and output never overlap (we hold a reference
        // to the output while the lock is released).
        m_samples_mutex = new ReentrantLock();
        m_samples_not_empty = m_samples_mutex.newCondition();

        m_samples = new Sample[kSamplesDepth + 2];
        for (int i = 0; i < kSamplesDepth + 2; i++) {
            m_samples[i] = new Sample();
        }

        // Configure interrupt on MXP DIO0
        m_interrupt = new DigitalInput(10);
        // Configure SPI bus for DMA read
        m_spi.initAuto(8200);
        m_spi.setAutoTransmitData(new byte[]{kGLOB_CMD}, 27);
        m_spi.startAutoTrigger(m_interrupt, true, false);

        m_freed.set(false);
        m_acquire_task = new Thread(new AcquireTask(this));
        m_acquire_task.setDaemon(true);
        m_acquire_task.start();

        // Start AHRS processing
        m_calculate_task = new Thread(new CalculateTask(this));
        m_calculate_task.setDaemon(true);
        m_calculate_task.start();

        calibrate();

        // Report usage and post data to DS

        HAL.report(tResourceType.kResourceType_ADIS16448, 0);
        setName("GuttedADIS");
    }

    /*
     * Constructor assuming yaw axis is "Z" and Complementary AHRS algorithm.
     */
    public GuttedADIS() {
        this(Axis.kZ);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void calibrate() {
        if (m_spi == null) return;

        Timer.delay(0.1);

        synchronized (this) {
            m_accum_count = 0;
            m_accum_gyro_x = 0.0;
            m_accum_gyro_y = 0.0;
            m_accum_gyro_z = 0.0;
        }

        Timer.delay(kCalibrationSampleTime);

        synchronized (this) {
            m_gyro_offset_x = m_accum_gyro_x / m_accum_count;
            m_gyro_offset_y = m_accum_gyro_y / m_accum_count;
            m_gyro_offset_z = m_accum_gyro_z / m_accum_count;
        }
    }

    static int ToUShort(ByteBuffer buf) {
        return (buf.getShort(0)) & 0xFFFF;
    }

    static int ToUShort(int... data) {
        ByteBuffer buf = ByteBuffer.allocateDirect(data.length);
        for (int d : data) {
            buf.put((byte) d);
        }
        return ToUShort(buf);
    }

    public static long ToULong(int sint) {
        return sint & 0x00000000FFFFFFFFL;
    }

    private static int ToShort(int... buf) {
        return (short) (((short) buf[0]) << 8 | buf[1]);
    }

    static int ToShort(ByteBuffer buf) {
        return ToShort(buf.get(0), buf.get(1));
    }

    private int readRegister(int reg) {
        ByteBuffer buf = ByteBuffer.allocateDirect(2);
        buf.order(ByteOrder.BIG_ENDIAN);
        buf.put(0, (byte) (reg & 0x7f));
        buf.put(1, (byte) 0);

        m_spi.write(buf, 2);
        m_spi.read(false, buf, 2);

        return ToUShort(buf);
    }

    private void writeRegister(int reg, int val) {
        ByteBuffer buf = ByteBuffer.allocateDirect(2);
        // low byte
        buf.put(0, (byte) ((0x80 | reg) | 0x10));
        buf.put(1, (byte) (val & 0xff));
        m_spi.write(buf, 2);
        // high byte
        buf.put(0, (byte) (0x81 | reg));
        buf.put(1, (byte) (val >> 8));
        m_spi.write(buf, 2);
    }

    private void printBytes(int[] data) {
        for (int i = 0; i < data.length; ++i) {
            System.out.print(data[i] + " ");
        }
        System.out.println();
    }

    private void printBytes(byte[] data) {
        for (int i = 0; i < data.length; ++i) {
            System.out.print(data[i] + " ");
        }
        System.out.println();
    }

    /**
     * {@inheritDoc}
     */
    public void reset() {
        synchronized (this) {
            m_integ_gyro_x = 0.0;
            m_integ_gyro_y = 0.0;
            m_integ_gyro_z = 0.0;
        }
    }

    @Override
    public double getAngle() {
        return getAngleX();
    }

    @Override
    public double getRate() {
        return getRateX();
    }

    /**
     * Delete (free) the spi port used for the IMU.
     */
    @Override
    public void free() {
        m_freed.set(true);
        if (m_samples_mutex != null) {
            m_samples_mutex.lock();
            try {
                m_samples_not_empty.signal();
            } finally {
                m_samples_mutex.unlock();
            }
        }
        try {
            if (m_acquire_task != null) {
                m_acquire_task.join();
            }
            if (m_calculate_task != null) {
                m_calculate_task.join();
            }
        } catch (InterruptedException e) {
        }
        if (m_interrupt != null) {
            m_interrupt.free();
            m_interrupt = null;
        }
        if (m_spi != null) {
            m_spi.free();
            m_spi = null;
        }
    }

    private void acquire() {
        ByteBuffer readBuf = ByteBuffer.allocateDirect(64000);
        readBuf.order(ByteOrder.LITTLE_ENDIAN);
        double gyro_x, gyro_y, gyro_z, accel_x, accel_y, accel_z, mag_x, mag_y, mag_z, baro, temp;
        int data_count = 0;
        int array_offset = 0;
        int imu_crc = 0;
        double dt = 0; // This number must be adjusted if decimation setting is changed. Default is 1/102.4 SPS
        int data_subset[] = new int[28];
        long timestamp_new = 0;
        int data_to_read = 0;

        while (!m_freed.get()) {
            // Waiting for the buffer to fill...
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
            } // A delay less than 10ms could potentially overflow the local buffer

            data_count = m_spi.readAutoReceivedData(readBuf, 0, 0); // Read number of bytes currently stored in the buffer
            array_offset = data_count % 116; // Look for "extra" data This is 116 not 29 like in C++ b/c everything is 32-bits and takes up 4 bytes in the buffer
            data_to_read = data_count - array_offset; // Discard "extra" data
            m_spi.readAutoReceivedData(readBuf, data_to_read, 0); // Read data from DMA buffer
            for (int i = 0; i < data_to_read; i += 116) { // Process each set of 28 bytes (timestamp + 28 data) * 4 (32-bit ints)
                for (int j = 1; j < 29; j++) { // Split each set of 28 bytes into a sub-array for processing
                    int at = (i + 4 * (j));
                    data_subset[j - 1] = readBuf.getInt(at);
                }
                // Calculate CRC-16 on each data packet
                int calc_crc = 0x0000FFFF; // Starting word
                int read_byte = 0;
                for (int k = 4; k < 26; k += 2) { // Cycle through XYZ GYRO, XYZ ACCEL, XYZ MAG, BARO, TEMP (Ignore Status & CRC)
                    read_byte = data_subset[k + 1]; // Process LSB
                    calc_crc = (calc_crc >>> 8) ^ adiscrc[(calc_crc & 0x000000FF) ^ read_byte];
                    read_byte = data_subset[k]; // Process MSB
                    calc_crc = (calc_crc >>> 8) ^ adiscrc[(calc_crc & 0x000000FF) ^ read_byte];
                }

                // Make sure to mask all but relevant 16 bits
                calc_crc = ~calc_crc & 0xFFFF;
                calc_crc = ((calc_crc << 8) | (calc_crc >> 8)) & 0xFFFF;
                //System.out.println("Calc: " + calc_crc);

                // This is the data needed for CRC
                ByteBuffer bBuf = ByteBuffer.allocateDirect(2);
                bBuf.put((byte) readBuf.getInt((i + 26) * 4 + 4)); // (i + 26) * 4 = position (32-bit ints) + 4 to skip timestamp
                bBuf.put((byte) readBuf.getInt((i + 27) * 4 + 4)); // (i + 27) * 4 = position (32-bit ints) + 4 to skip timestamp

                imu_crc = ToUShort(bBuf); // Extract DUT CRC from data
                //System.out.println("IMU: " + imu_crc);
                //System.out.println("------------");

                // Compare calculated vs read CRC. Don't update outputs if CRC-16 is bad
                if (calc_crc == imu_crc) {
                    // Calculate delta-time (dt) using FPGA timestamps
                    timestamp_new = ToULong(readBuf.getInt(i * 4));
                    dt = (timestamp_new - timestamp_old) / 1000000.0; // Calculate dt and convert us to seconds
                    timestamp_old = timestamp_new; // Store new timestamp in old variable for next cycle

                    gyro_x = ToShort(data_subset[4], data_subset[5]) * kDegreePerSecondPerLSB;
                    gyro_y = ToShort(data_subset[6], data_subset[7]) * kDegreePerSecondPerLSB;
                    gyro_z = ToShort(data_subset[8], data_subset[9]) * kDegreePerSecondPerLSB;
                    accel_x = ToShort(data_subset[10], data_subset[11]) * kGPerLSB;
                    accel_y = ToShort(data_subset[12], data_subset[13]) * kGPerLSB;
                    accel_z = ToShort(data_subset[14], data_subset[15]) * kGPerLSB;
                    mag_x = ToShort(data_subset[16], data_subset[17]) * kMilligaussPerLSB;
                    mag_y = ToShort(data_subset[18], data_subset[19]) * kMilligaussPerLSB;
                    mag_z = ToShort(data_subset[20], data_subset[21]) * kMilligaussPerLSB;
                    baro = ToUShort(data_subset[22], data_subset[23]) * kMillibarPerLSB;
                    temp = ToShort(data_subset[24], data_subset[25]) * kDegCPerLSB + kDegCOffset;

                    // Print scaled data to terminal
          /*System.out.println(gyro_x + "," + gyro_y + "," + gyro_z + "," + accel_x + "," + accel_y + ","
          + accel_z + "," + mag_x + "," + mag_y + "," + mag_z + "," + baro + "," + temp + "," + ","
          + ToUShort(data_subset[26], data_subset[27]));*/
                    //System.out.println("---------------------"); // Frame divider (or else data looks like a mess)

                    m_samples_mutex.lock();
                    try {
                        // If the FIFO is full, just drop it
                        if (m_calculate_started && m_samples_count < kSamplesDepth) {
                            Sample sample = m_samples[m_samples_put_index];
                            sample.gyro_x = gyro_x;
                            sample.gyro_y = gyro_y;
                            sample.gyro_z = gyro_z;
                            sample.accel_x = accel_x;
                            sample.accel_y = accel_y;
                            sample.accel_z = accel_z;
                            sample.mag_x = mag_x;
                            sample.mag_y = mag_y;
                            sample.mag_z = mag_z;
                            sample.baro = baro;
                            sample.temp = temp;
                            sample.dt = dt;
                            ++m_samples_put_index;
                            if (m_samples_put_index == (kSamplesDepth + 2))
                                m_samples_put_index = 0;
                            ++m_samples_count;
                            m_samples_not_empty.signal();
                        }
                    } catch (Exception e) {
                        break;
                    } finally {
                        m_samples_mutex.unlock();
                    }

                    // Update global state
                    synchronized (this) {
                        m_gyro_x = gyro_x;
                        m_gyro_y = gyro_y;
                        m_gyro_z = gyro_z;

                        ++m_accum_count;
                        m_accum_gyro_x += gyro_x;
                        m_accum_gyro_y += gyro_y;
                        m_accum_gyro_z += gyro_z;

                        m_integ_gyro_x += (gyro_x - m_gyro_offset_x) * dt;
                        m_integ_gyro_y += (gyro_y - m_gyro_offset_y) * dt;
                        m_integ_gyro_z += (gyro_z - m_gyro_offset_z) * dt;
                    }
                } else {
                    System.out.println("Invalid CRC");
                }
            }
        }
    }

    private void calculate() {
        while (!m_freed.get()) {
            // Wait for next sample and get it
            try {
                Thread.sleep(20);
            } catch (InterruptedException ignored) {
            }
            Sample sample;
            m_samples_mutex.lock();
            try {
                m_calculate_started = true;
                while (m_samples_count == 0) {
                    m_samples_not_empty.await();
                    if (m_freed.get()) {
                        return;
                    }
                }
                sample = m_samples[m_samples_take_index];
                ++m_samples_take_index;
                if (m_samples_take_index == (kSamplesDepth + 2))
                    m_samples_take_index = 0;
                --m_samples_count;
            } catch (InterruptedException e) {
                break;
            } finally {
                m_samples_mutex.unlock();
            }
        }
    }

    public synchronized double getAngleX() {
        return m_integ_gyro_x;
    }

    public synchronized double getAngleY() {
        return m_integ_gyro_y;
    }

    public synchronized double getAngleZ() {
        return m_integ_gyro_z;
    }

    public synchronized double getRateX() {
        return m_gyro_x;
    }

    public synchronized double getRateY() {
        return m_gyro_y;
    }

    public synchronized double getRateZ() {
        return m_gyro_z;
    }

    public synchronized double getLastSampleTime() {
        return m_last_sample_time;
    }

}

