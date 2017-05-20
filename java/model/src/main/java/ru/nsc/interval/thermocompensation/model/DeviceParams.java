package ru.nsc.interval.thermocompensation.model;

/**
 * Definition of the thermocompensation device parameters. Some of this
 * parameters are used by the polynomial evaluator: INF, SBIT, K1BIT, K2BIT,
 * K3BIT, K4BIT, K5BIT . Others are more special.
 */
public class DeviceParams {

    // Field width in bits
    public static final int INF_SIZE = 6;
    public static final int SBIT_SIZE = 5;
    public static final int K1BIT_SIZE = 8;
    public static final int K2BIT_SIZE = 7;
    public static final int K3BIT_SIZE = 5;
    public static final int K4BIT_SIZE = 5;
    public static final int K5BIT_SIZE = 4;
    public static final int CDACF_SIZE = 6;
    public static final int CDACC_SIZE = 4;
    public static final int DIV_SIZE = 1;
    public static final int SENSE_SIZE = 3;
    public static final int TOTAL_SIZE = INF_SIZE + SBIT_SIZE
            + K1BIT_SIZE + K2BIT_SIZE + K3BIT_SIZE + K4BIT_SIZE + K5BIT_SIZE
            + CDACF_SIZE + CDACC_SIZE + DIV_SIZE + SENSE_SIZE;
    // Fields
    public int INF;
    public int SBIT;
    public int K1BIT;
    public int K2BIT;
    public int K3BIT;
    public int K4BIT;
    public int K5BIT;
    public int CDACF;
    public int CDACC;
    public int DIV;
    public int SENSE;

    /**
     * Self-check. Check that all fields fits in their bit length
     */
    public void check() {
        check(INF, INF_SIZE);
        check(SBIT, SBIT_SIZE);
        check(K1BIT, K1BIT_SIZE);
        check(K2BIT, K2BIT_SIZE);
        check(K3BIT, K3BIT_SIZE);
        check(K4BIT, K4BIT_SIZE);
        check(K5BIT, K5BIT_SIZE);
        check(CDACF, CDACF_SIZE);
        check(CDACC, CDACC_SIZE);
        check(DIV, DIV_SIZE);
        check(SENSE, SENSE_SIZE);
    }

    /**
     * Pack DeviceParams to packed bits
     *
     * @returns packed bits
     */
    public long toLong() {
        check();
        long v = 0;
        v = (v << INF_SIZE) | INF;
        v = (v << SBIT_SIZE) | SBIT;
        v = (v << K1BIT_SIZE) | K1BIT;
        v = (v << K2BIT_SIZE) | K2BIT;
        v = (v << K3BIT_SIZE) | K3BIT;
        v = (v << K4BIT_SIZE) | K4BIT;
        v = (v << K5BIT_SIZE) | K5BIT;
        v = (v << CDACF_SIZE) | CDACF;
        v = (v << CDACC_SIZE) | CDACC;
        v = (v << DIV_SIZE) | DIV;
        v = (v << SENSE_SIZE) | SENSE;
        long rev = 0;
        for (int i = 0; i < TOTAL_SIZE; i++) {
            rev <<= 1;
            if ((v & 1) != 0) {
                rev |= 1;
            }
            v >>= 1;
        }
        assert v == 0;
        return rev;
    }

    /**
     * Unpack DeviceParams from packed bits
     *
     * @param r packed bits
     */
    public void fromLong(long r) {
        long v = 0;
        for (int i = 0; i < TOTAL_SIZE; i++) {
            v <<= 1;
            if ((r & 1) != 0) {
                v |= 1;
            }
            r >>= 1;
        }
        assert r == 0;
        SENSE = (int) (v & ((1 << SENSE_SIZE) - 1));
        v >>= SENSE_SIZE;
        DIV = (int) (v & ((1 << DIV_SIZE) - 1));
        v >>= DIV_SIZE;
        CDACC = (int) (v & ((1 << CDACC_SIZE) - 1));
        v >>= CDACC_SIZE;
        CDACF = (int) (v & ((1 << CDACF_SIZE) - 1));
        v >>= CDACF_SIZE;
        K5BIT = (int) (v & ((1 << K5BIT_SIZE) - 1));
        v >>= K5BIT_SIZE;
        K4BIT = (int) (v & ((1 << K4BIT_SIZE) - 1));
        v >>= K4BIT_SIZE;
        K3BIT = (int) (v & ((1 << K3BIT_SIZE) - 1));
        v >>= K3BIT_SIZE;
        K2BIT = (int) (v & ((1 << K2BIT_SIZE) - 1));
        v >>= K2BIT_SIZE;
        K1BIT = (int) (v & ((1 << K1BIT_SIZE) - 1));
        v >>= K1BIT_SIZE;
        SBIT = (int) (v & ((1 << SBIT_SIZE) - 1));
        v >>= SBIT_SIZE;
        INF = (int) (v & ((1 << INF_SIZE) - 1));
        v >>= INF_SIZE;
        assert v == 0;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof DeviceParams) {
            DeviceParams that = (DeviceParams) o;
            return this.INF == that.INF
                    && this.SBIT == that.SBIT
                    && this.K1BIT == that.K1BIT
                    && this.K2BIT == that.K2BIT
                    && this.K3BIT == that.K3BIT
                    && this.K4BIT == that.K4BIT
                    && this.K5BIT == that.K5BIT
                    && this.CDACF == that.CDACF
                    && this.CDACC == that.CDACC
                    && this.DIV == that.DIV
                    && this.SENSE == that.SENSE;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + this.INF;
        hash = 67 * hash + this.SBIT;
        hash = 67 * hash + this.K1BIT;
        hash = 67 * hash + this.K2BIT;
        hash = 67 * hash + this.K3BIT;
        hash = 67 * hash + this.K4BIT;
        hash = 67 * hash + this.K5BIT;
        hash = 67 * hash + this.CDACF;
        hash = 67 * hash + this.CDACC;
        hash = 67 * hash + this.DIV;
        hash = 67 * hash + this.SENSE;
        return hash;
    }

    /**
     * Convert DeviceParams to a list of bit fields
     *
     * @return A string with a list of bit fields
     */
    public String toBitString() {
        StringBuilder sb = new StringBuilder();
        appBitString(sb, INF, INF_SIZE);
        sb.append(" ");
        appBitString(sb, SBIT, SBIT_SIZE);
        sb.append(" ");
        appBitString(sb, K1BIT, K1BIT_SIZE);
        sb.append(" ");
        appBitString(sb, K2BIT, K2BIT_SIZE);
        sb.append(" ");
        appBitString(sb, K3BIT, K3BIT_SIZE);
        sb.append(" ");
        appBitString(sb, K4BIT, K4BIT_SIZE);
        sb.append(" ");
        appBitString(sb, K5BIT, K5BIT_SIZE);
        sb.append(" ");
        appBitString(sb, CDACF, CDACF_SIZE);
        sb.append(" ");
        appBitString(sb, CDACC, CDACC_SIZE);
        sb.append(" ");
        appBitString(sb, DIV, DIV_SIZE);
        sb.append(" ");
        appBitString(sb, SENSE, SENSE_SIZE);
        return sb.toString();
    }

    public static DeviceParams fromNom(String s) {
        String[] ss = s.trim().split(" +");
        assert ss.length == 11;
        DeviceParams dp = new DeviceParams();
        dp.INF = Integer.valueOf(ss[0]);
        dp.SBIT = Integer.valueOf(ss[1]);
        dp.K1BIT = Integer.valueOf(ss[2]);
        dp.K2BIT = Integer.valueOf(ss[3]);
        dp.K3BIT = Integer.valueOf(ss[4]);
        dp.K4BIT = Integer.valueOf(ss[5]);
        dp.K5BIT = Integer.valueOf(ss[6]);
        dp.CDACF = Integer.valueOf(ss[7]);
        dp.CDACC = Integer.valueOf(ss[8]);
        dp.DIV = Integer.valueOf(ss[9]);
        dp.SENSE = Integer.valueOf(ss[10]);
        dp.check();
        return dp;
    }

    /**
     * Append binary representation of integer value to a StringBuilder
     *
     * @param sb StringBuilder
     * @param v integer value
     * @param nBits number of bits
     */
    private static void appBitString(StringBuilder sb, int v, int nBits) {
        for (int i = nBits - 1; i >= 0; i--) {
            sb.append(((v >> i) & 1) != 0 ? '1' : '0');
        }
    }

    /**
     * Check that an integer value fits in certain number of bits
     *
     * @param val integer value
     * @param nBits number of bits
     */
    private static void check(int val, int nBits) {
        int mask = (1 << nBits) - 1;
        assert (val & ~mask) == 0;
    }
}
