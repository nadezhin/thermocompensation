package ru.nsc.interval.thermocompensation.model;

/**
 * Low-level model of the polynomial evaluator. It is a finite state machine
 * with state PolyStae and input Inp. Initial state is defined by reset()
 * method. There two state transition methods: negedge corresponds to falling (
 * 1 --> 0) edge of CLK posedge corresponds to falling ( 1 --> 0) edge of CLK
 * They create new state from previous state and input
 */
public class PolyState {

    /**
     * Input of the polynomial evaluator.
     */
    public static class Inp {

        /**
         * Clock signal
         */
        public int CLK;
        /**
         * Reset signal
         */
        public int RST;
        /**
         * First polinomial coefficient from the read-only memory
         */
        public int K1BIT;
        /**
         * Second polinomial coefficient from the read-only memory
         */
        public int K2BIT;
        /**
         * Third polinomial coefficient from the read-only memory
         */
        public int K3BIT;
        /**
         * Forth polinomial coefficient from the read-only memory
         */
        public int K4BIT;
        /**
         * Fifth polinomial coefficient from the read-only memory
         */
        public int K5BIT;
        /**
         * Scaling coefficient from the read-only memory
         */
        public int SBIT;
        /**
         * Bias coefficient from the read-only memory
         */
        public int INF;
        /**
         * Temperature value - input of the polynomial evaluator
         */
        public int T;
        /**
         * Signal to start normal computation of the polinomial.
         */
        public int ENwork;
        /**
         * Signal to start diagnostic computation of the polinomial.
         */
        public int ENshift;
        /**
         * Fine capacitance coefficient from the read-only memory. Not used by
         * the polynomial evaluator.
         */
        public int CF;
        /**
         * Rough capacitance coefficient from the read-only memory. Not used by
         * the polynomial evaluator.
         */
        public int CC;
        /**
         * Frequence divide flag from the read-only memory. Not used by the
         * polynomial evaluator.
         */
        public int DIV;
        /**
         * Sense coefficient from the read-only memory. Not used by the
         * polynomial evaluator.
         */
        public int SENSE;

        Inp() {
        }

        public Inp(Inp that) {
            CLK = that.CLK;
            RST = that.RST;
            K1BIT = that.K1BIT;
            K2BIT = that.K2BIT;
            K3BIT = that.K3BIT;
            K4BIT = that.K4BIT;
            K5BIT = that.K5BIT;
            SBIT = that.SBIT;
            INF = that.INF;
            T = that.T;
            ENwork = that.ENwork;
            ENshift = that.ENshift;
            CF = that.CF;
            CC = that.CC;
            DIV = that.DIV;
            SENSE = that.SENSE;
        }

        public Inp(DeviceParams dp) {
            ENwork = 1;
            INF = dp.INF;
            SBIT = dp.SBIT;
            K1BIT = dp.K1BIT;
            K2BIT = dp.K2BIT;
            K3BIT = dp.K3BIT;
            K4BIT = dp.K4BIT;
            K5BIT = dp.K5BIT;
            CF = dp.CDACF;
            CC = dp.CDACC;
            DIV = dp.DIV;
            SENSE = dp.SENSE;
        }

        public String toNom() {
            StringBuilder sb = new StringBuilder();
            sb.append(' ');
            sb.append(INF);
            sb.append(' ');
            sb.append(SBIT);
            sb.append(' ');
            sb.append(K1BIT);
            sb.append(' ');
            sb.append(K2BIT);
            sb.append(' ');
            sb.append(K3BIT);
            sb.append(' ');
            sb.append(K4BIT);
            sb.append(' ');
            sb.append(K5BIT);
            sb.append(' ');
            sb.append(CF);
            sb.append(' ');
            sb.append(CC);
            sb.append(' ');
            sb.append(DIV);
            sb.append(' ');
            sb.append(SENSE);
            return sb.toString();
        }

        public String toLongNom() {
            StringBuilder sb = new StringBuilder();
            sb.append("inf=");
            sb.append(INF);
            sb.append(" sc=");
            sb.append(SBIT);
            sb.append(" k1=");
            sb.append(K1BIT);
            sb.append(" k2=");
            sb.append(K2BIT);
            sb.append(" k3=");
            sb.append(K3BIT);
            sb.append(" k4=");
            sb.append(K4BIT);
            sb.append(" k5=");
            sb.append(K5BIT);
            sb.append(" cf=");
            sb.append(CF);
            sb.append(" cc=");
            sb.append(CC);
            sb.append(" div=");
            sb.append(DIV);
            sb.append(" sen=");
            sb.append(SENSE);
            return sb.toString();
        }

        public DeviceParams toDeviceParams() {
            DeviceParams dp = new DeviceParams();
            dp.INF = INF;
            dp.SBIT = SBIT;
            dp.K1BIT = K1BIT;
            dp.K2BIT = K2BIT;
            dp.K3BIT = K3BIT;
            dp.K4BIT = K4BIT;
            dp.K5BIT = K5BIT;
            dp.CDACF = CF;
            dp.CDACC = CC;
            dp.DIV = DIV;
            dp.SENSE = SENSE;
            return dp;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof PolyState.Inp) {
                PolyState.Inp that = (PolyState.Inp) o;
                return this.INF == that.INF
                        && this.SBIT == that.SBIT
                        && this.K1BIT == that.K1BIT
                        && this.K2BIT == that.K2BIT
                        && this.K3BIT == that.K3BIT
                        && this.K4BIT == that.K4BIT
                        && this.K5BIT == that.K5BIT
                        && this.CF == that.CF
                        && this.CC == that.CC
                        && this.DIV == that.DIV
                        && this.SENSE == that.SENSE;
            }
            return false;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 71 * hash + this.K1BIT;
            hash = 71 * hash + this.K2BIT;
            hash = 71 * hash + this.K3BIT;
            hash = 71 * hash + this.K4BIT;
            hash = 71 * hash + this.K5BIT;
            hash = 71 * hash + this.SBIT;
            hash = 71 * hash + this.INF;
            hash = 71 * hash + this.CF;
            hash = 71 * hash + this.CC;
            hash = 71 * hash + this.DIV;
            hash = 71 * hash + this.SENSE;
            return hash;
        }

        /**
         * Создать номинальный набор параметров цифровой микросборки. Поля
         * объекта можно менять.
         *
         * @return
         */
        public static PolyState.Inp genNom() {
            PolyState.Inp nom = new PolyState.Inp();
            nom.INF = 31;
            nom.SBIT = 15;
            nom.K1BIT = 127;
            nom.K2BIT = 63;
            nom.K3BIT = 15;
            nom.K4BIT = 15;
            nom.K5BIT = 7;
            nom.CF = 0;
            nom.CC = 0;
            nom.DIV = 0;
            nom.SENSE = 7;
            return nom;
        }
    }
    // Components of the polynomial evaluator state
    int RESULTout;
    int XS;
    long RESULT;
    int WORK;
    int P;
//    int SL;
    int P1;
//    int SL2;
    int P2;
    int RESULTn_1, RESULTn_2;
    int CNTP;
    int CNTS;
    int CNTM;
    int CNTD;
    int DONE;
    int ENwork_trig0;
    int ENwork_trig1;
    int ENshift_trig0;
    int ENshift_trig1;
    int ENshift_trig2;
    int ENshift_trig3;
    int ENshift_trig4;
    int EN_trig;

    /**
     * Method reflects the RST input signal. It sets the state to initial state.
     */
    void reset() {
        ENshift_trig0 = 0;
        ENshift_trig1 = 0;
        ENshift_trig2 = 0;
        ENshift_trig3 = 0;
        ENshift_trig4 = 0;

        ENwork_trig0 = 0;
        ENwork_trig1 = 0;
        EN_trig = 0;

        CNTP = 0;
        DONE = 0;
        CNTS = 12;
        CNTM = 34;
        CNTD = 0;

        P = 0;
        P1 = 0;
        P2 = 0;

        XS = 0b0000000000010;

        RESULTn_1 = 0;
        RESULTn_2 = 0;

        RESULT = 0b11111_1111111111_1111111111_1111111111L;
        WORK = 0;
        RESULTout = 0;
    }

    /**
     * Convert long value to a bit string
     *
     * @param v value
     * @param n number of bits
     * @return text string with all chars '0' or '1'
     */
    static String toBitString(long v, int n) {
        long mask = (1L << n) - 1;
        assert (v & ~mask) == 0;
        StringBuilder sb = new StringBuilder();
        do {
            n--;
            sb.append(((v >> n) & 1) != 0 ? '1' : '0');
        } while (n > 0);
        return sb.toString();
    }

    /**
     * Show main components of the state
     */
    void show() {
        System.out.println("=== ENwork_trig1=" + ENwork_trig1 + " EN_trig=" + EN_trig + " CNTS=" + CNTS + " CNTM=" + CNTM + " CNTP=" + CNTP);
        System.out.println(" RESULT=" + toBitString(RESULT, 35));
        System.out.println(" WORK=" + toBitString(WORK, 13));
        System.out.println(" XS=" + toBitString(XS, 13));
        System.out.println(" RESULTn_1=" + RESULTn_1 + " RESULTn_2=" + RESULTn_2 + " P=" + P + " P1=" + P1 + " P2=" + P2);
    }

    /**
     * Method reflects the falling edge of th clock signal CLK. It creates new
     * state from input and old state.
     *
     * @param inp input
     * @param old old state
     */
    void negedge(Inp inp, PolyState old) {
        ENshift_trig0 = old.ENshift_trig0;
        ENshift_trig1 = old.ENshift_trig1;
        ENshift_trig2 = old.ENshift_trig2;
        ENshift_trig3 = old.ENshift_trig3;
        ENshift_trig4 = old.ENshift_trig4;
        ENwork_trig0 = old.ENwork_trig0;
        ENwork_trig1 = old.ENwork_trig1;
        EN_trig = old.EN_trig;

        //====================COUNTERS===========================;
        if ((old.EN_trig != 0) & (old.CNTS == 0) & (old.CNTM == 0)) {
            CNTP = old.CNTP + 1;
        } else if (old.EN_trig == 0) {
            CNTP = 0;
        } else {
            CNTP = old.CNTP;
        }

        if ((old.EN_trig != 0) & (old.CNTS == 0) & (old.CNTM == 0)) {
            DONE = (old.CNTP == 6) ? 1 : 0;
        } else if (old.EN_trig == 0) {
            DONE = 0;
        } else {
            DONE = old.DONE;
        }

        if (old.EN_trig == 0) {
            CNTS = 12;
        } else if ((old.EN_trig != 0) & (old.DONE == 0) & (old.CNTS == 0)) {
            CNTS = 12;
        } else if ((old.EN_trig != 0) & (old.DONE == 0)) {
            CNTS = old.CNTS - 1;
        } else {
            CNTS = old.CNTS;
        }

        if (old.EN_trig == 0) {
            CNTM = 34;
            CNTD = 0;
        } else if ((old.EN_trig != 0) & (old.DONE == 0) & (old.CNTS == 0) & (old.CNTM != 0)) {
            CNTM = old.CNTM - 1;
            CNTD = old.CNTD;
        } else if ((old.EN_trig != 0) & (old.DONE == 0) & (old.CNTS == 0) & (old.CNTM == 0)) {
            switch (old.CNTP) {
                case 0:
                    CNTM = 34 + 1;
                    CNTD = 0;
                    break; //+1 bacause xs latch data one takt later;
                case 1:
                    CNTM = 34;
                    CNTD = 0;
                    break; //Z0 //1:begin CNTM  <=	34;	CNTD <= 0; end
                case 2:
                    CNTM = 34 + 10;
                    CNTD = 10;
                    break;	//Z1
                case 3:
                    CNTM = 34 + 10;
                    CNTD = 10;
                    break;	//Z2
                case 4:
                    CNTM = 34 + 10;
                    CNTD = 10;
                    break; //Z3
                case 5:
                    CNTM = 34 + 14;
                    CNTD = 14;
                    break; //Z41
                case 6:
                    CNTM = 0;
                    CNTD = 0;
                    break; //Z4
                default:
                    CNTM = old.CNTM;
                    CNTD = old.CNTD;
            }
        } else {
            CNTM = old.CNTM;
            CNTD = old.CNTD;
        }

        P = old.P;
        P1 = old.P1;
        P2 = old.P2;
        XS = old.XS;
        RESULTn_1 = old.RESULTn_1;
        RESULTn_2 = old.RESULTn_2;
        RESULT = old.RESULT;
        WORK = old.WORK;
        RESULTout = old.RESULTout;
    }

    /**
     * Method reflects the rising edge of th clock signal CLK. It creates new
     * state from input and old state.
     *
     * @param inp input
     * @param old old state
     * @param fixBug model behaves as if the bug in the evaluator is fixed
     */
    void posedge(Inp inp, PolyState old, boolean fixBugP) {
        ENshift_trig0 = inp.ENshift;
        ENshift_trig1 = old.ENshift_trig0;
        ENshift_trig2 = old.ENshift_trig1;
        ENshift_trig3 = old.ENshift_trig2;
        ENshift_trig4 = old.ENshift_trig3;

        ENwork_trig0 = inp.ENwork;
        ENwork_trig1 = old.ENwork_trig0;

        int PAUSE = old.ENshift_trig1 & ~old.ENshift_trig4;
        int EN = (old.ENwork_trig1 | old.ENshift_trig4) & ~PAUSE;
        EN_trig = EN;

        //====================COUNTERS===========================;
        CNTP = old.CNTP;
        DONE = old.DONE;
        CNTS = old.CNTS;
        CNTM = old.CNTM;
        CNTD = old.CNTD;

        //=====================MULTIPLIER===========================;
        int m = (old.CNTM >= old.CNTD) ? (bit(old.RESULT, 0) & ~old.RESULTn_1) : (old.RESULTn_1 & ~old.RESULTn_1);
        int p = (old.CNTM >= old.CNTD) ? (~bit(old.RESULT, 0) & old.RESULTn_1) : (~old.RESULTn_1 & old.RESULTn_2);

        int S1in = m != 0 ? bit(~old.XS, 0) : (p != 0 ? bit(old.XS, 0) : 0);
        int S2in = bit(old.WORK, 0);
        int Pin = (old.CNTS == 12) ? m : old.P;

        int S = S1in ^ S2in ^ Pin;
        if ((EN != 0) & (old.DONE == 0)) {
            P = S1in & S2in & ~Pin | S1in & ~S2in & Pin | ~S1in & S2in & Pin | S1in & S2in & Pin;
        } else if (EN == 0) {
            P = 0;
        } else {
            P = old.P;
        }
//        System.out.println(" m=" + m + " p=" + p + " S1in=" + S1in + " S2in=" + S2in + " Pin=" + Pin + " S=" + S + " P=" + P);

        //====================SUMMATOR1===============================;
        int SL;
        switch (old.CNTP) {
            case 0:
                SL = 0;
                break;
            case 1:
                switch (old.CNTM) {
                    case 31:
                        SL = 1;
                        break;
                    default:
                        SL = 0;
                }
                break;
            case 2:
                switch (old.CNTM) {
                    case 35:
                        SL = 1;
                        break;
                    case 34 - 9:
                        SL = bit(inp.K4BIT, 0);
                        break; //BIT #0:  (RES_Size -1)(34) + REZ_shift(0) - Kshift(9));
                    case 33 - 9:
                        SL = bit(inp.K4BIT, 1);
                        break;
                    case 32 - 9:
                        SL = bit(inp.K4BIT, 2);
                        break;
                    case 31 - 9:
                        SL = bit(inp.K4BIT, 3);
                        break;
                    case 30 - 9:
                        SL = bit(inp.K4BIT, 4);
                        break;
                    default:
                        SL = 0;
                }
                break;
            case 3:
                switch (old.CNTM) {
                    case 35:
                        SL = 1;
                        break;
                    case 34 + 10 - 15 - 4:
                        SL = bit(inp.K3BIT, 0);
                        break;//BIT #0:  (RES_Size -1)(34) + REZ_shift(10) - Kshift(15) - fraction(4);
                    case 33 + 10 - 15 - 4:
                        SL = bit(inp.K3BIT, 1);
                        break;
                    case 32 + 10 - 15 - 4:
                        SL = bit(inp.K3BIT, 2);
                        break;
                    case 31 + 10 - 15 - 4:
                        SL = bit(inp.K3BIT, 3);
                        break;
                    case 30 + 10 - 15 - 4:
                        SL = bit(inp.K3BIT, 4);
                        break;
                    default:
                        SL = 0;
                }
                break;
            case 4:
                switch (old.CNTM) {
                    case 35:
                        SL = 1;
                        break;
                    case 34 + 10 - 13 - 4:
                        SL = bit(inp.K2BIT, 0);
                        break;//BIT #0:  (RES_Size -1)(34) + REZ_shift(10) - Kshift(13) - fraction(4);
                    case 33 + 10 - 13 - 4:
                        SL = bit(inp.K2BIT, 1);
                        break;
                    case 32 + 10 - 13 - 4:
                        SL = bit(inp.K2BIT, 2);
                        break;
                    case 31 + 10 - 13 - 4:
                        SL = bit(inp.K2BIT, 3);
                        break;
                    case 30 + 10 - 13 - 4:
                        SL = bit(inp.K2BIT, 4);
                        break;
                    case 29 + 10 - 13 - 4:
                        SL = bit(inp.K2BIT, 5);
                        break;
                    case 28 + 10 - 13 - 4:
                        SL = bit(inp.K2BIT, 6);
                        break;
                    default:
                        SL = 0;
                }
                break;
            case 5:
                switch (old.CNTM) {
                    case 34 + 10 - 13 - 4:
                        SL = bit(~inp.K1BIT, 0);
                        break;//BIT #0:  (RES_Size -1)(34) + REZ_shift(10) - Kshift(13) - fraction(4);
                    case 33 + 10 - 13 - 4:
                        SL = bit(~inp.K1BIT, 1);
                        break;
                    case 32 + 10 - 13 - 4:
                        SL = bit(~inp.K1BIT, 2);
                        break;
                    case 31 + 10 - 13 - 4:
                        SL = bit(~inp.K1BIT, 3);
                        break;
                    case 30 + 10 - 13 - 4:
                        SL = bit(~inp.K1BIT, 4);
                        break;
                    case 29 + 10 - 13 - 4:
                        SL = bit(~inp.K1BIT, 5);
                        break;
                    case 28 + 10 - 13 - 4:
                        SL = bit(~inp.K1BIT, 6);
                        break;
                    case 27 + 10 - 13 - 4:
                        SL = bit(~inp.K1BIT, 7);
                        break;
                    default:
                        SL = 1;
                }
                break;
            case 6:
                switch (old.CNTM) {
                    case 35:
                        SL = 1;
                        break;
                    default:
                        SL = 0;
                }
                break;
            default:
                SL = 0;
        }

        int S1 = bit(old.WORK, 1) ^ SL ^ old.P1;

        if (fixBugP) {
            if (EN == 0) {
                P1 = 0;
            } else if ((EN != 0) & (old.DONE == 0) & (old.CNTS == 0) & (old.CNTM == 0)) {
                P1 = 0;
            } else if ((EN != 0) & (old.DONE == 0) & (old.CNTS == 0)) {
                P1 = bit(old.WORK, 1) & SL & ~old.P1
                        | bit(old.WORK, 1) & ~SL & old.P1
                        | bit(~old.WORK, 1) & SL & old.P1
                        | bit(old.WORK, 1) & SL & old.P1;
            } else {
                P1 = old.P1;
            }
        } else if (EN == 0) {
            P1 = 0;
        } else if ((EN != 0) & (old.DONE == 0) & (old.CNTS == 0)) {
            P1 = bit(old.WORK, 1) & SL & ~old.P1
                    | bit(old.WORK, 1) & ~SL & old.P1
                    | bit(~old.WORK, 1) & SL & old.P1
                    | bit(old.WORK, 1) & SL & old.P1;
        } else if ((EN != 0) & (old.DONE == 0) & (old.CNTS == 0) & (old.CNTM == 0)) {
            P1 = 0;
        } else {
            P1 = old.P1;
        }
        if (old.CNTS == 0 && PolyModel.DEBUG >= 4) {
            System.out.println("m= " + m + " p=" + p + " SL=" + SL + " WORK[1]=" + bit(old.WORK, 1) + " P1=" + old.P1 + " S1=" + S1 + " P1=" + P1);
        }

        //====================SUMMATOR2===============================;
        int SL2;
        switch (old.CNTP) {
            case 0:
                switch (old.CNTM) {
                    case 33:
                    case 32:
                    case 31:
                    case 30:
                    case 29:
                    case 28:
                    case 27:
                    case 26:
                    case 24:
                        SL2 = 0;
                        break;
                    default:
                        SL2 = 1;
                }
                break;
            case 2:
                switch (old.CNTM) {
                    case 34:
                    case 33:
                    case 32:
                    case 31:
                    case 30:
                    case 29:
                    case 28:
                    case 27:
                    case 26:
                    case 22:
                    case 21:
                        SL2 = 0;
                        break;
                    default:
                        SL2 = 1;
                }
                break;
            case 3:
                switch (old.CNTM) {
                    case 34 + 10 - 18 - 4:
                        SL2 = 1;
                        break;//..0010_0000_0000_0000_0000
                    default:
                        SL2 = 0;
                }
                break;
            case 4:
                switch (old.CNTM) {
                    case 34 + 10 - 17 - 4:
                    case 34 + 10 - 15 - 4:
                        SL2 = 1;
                        break;//..0000_0010_1000_0000_0000_0000
                    default:
                        SL2 = 0;
                }
                break;
            case 5:
                switch (old.CNTM) {
                    case 34 + 10:
                    case 33 + 10:
                    case 32 + 10:
                    case 31 + 10:
                    case 34 + 10 - 4:
                    case 33 + 10 - 4:
                    case 32 + 10 - 4:
                    case 31 + 10 - 4:
                    case 30 + 10 - 4:
                    case 29 + 10 - 4:
                    case 28 + 10 - 4:
                    case 27 + 10 - 4:
                    case 26 + 10 - 4:
                    case 25 + 10 - 4:
                    case 24 + 10 - 4:
                    case 23 + 10 - 4:
                    case 21 + 10 - 4:
                    case 16 + 10 - 4:
                    case 15 + 10 - 4:
                        SL2 = 0;
                        break;//..11_0011_1101_0000_0000_0000
                    default:
                        SL2 = 1;
                }
                break;
            case 6:
                switch (old.CNTM) {
                    case 34 + 14 - 20 - 4:
                    case 34 + 14 - 13 - 4:
                        SL2 = 1;
                        break;//..0001_0000_0010_0000_0000_0000//1032
                    default:
                        SL2 = 0;
                }
                break;
            default:
                SL2 = 0;
        }

        int S2 = S1 ^ SL2 ^ old.P2;

        if (fixBugP) {
            if (EN == 0) {
                P2 = 0;
            } else if ((EN != 0) & (old.DONE == 0) & (old.CNTS == 0) & (old.CNTM == 0)) {
                P2 = 0;
            } else if ((EN != 0) & (old.DONE == 0) & (old.CNTS == 0)) {
                P2 = S1 & SL2 & ~old.P2 | S1 & ~SL2 & old.P2 | ~S1 & SL2 & old.P2 | S1 & SL2 & old.P2;
            } else {
                P2 = old.P2;
            }
        } else if (EN == 0) {
            P2 = 0;
        } else if ((EN != 0) & (old.DONE == 0) & (old.CNTS == 0)) {
            P2 = S1 & SL2 & ~old.P2 | S1 & ~SL2 & old.P2 | ~S1 & SL2 & old.P2 | S1 & SL2 & old.P2;
        } else if ((EN != 0) & (old.DONE == 0) & (old.CNTS == 0) & (old.CNTM == 0)) {
            P2 = 0;
        } else {
            P2 = old.P2;
        }
        if (old.CNTS == 0 && PolyModel.DEBUG >= 4) {
            System.out.println(" SL2=" + SL2 + " S1=" + S1 + " P2=" + old.P2 + " S2=" + S2 + " P2=" + P2);
        }

        //====================SHIFT REGISTERS========================;
        if (EN == 0) {
            XS = inp.INF << 3;
        } else if ((EN != 0) & (old.DONE == 0) & (old.CNTS == 0) & (old.CNTM == 0) & (old.CNTP == 0)) {
            XS = (bit(inp.SBIT, 4) << 5) | (bit(~inp.SBIT, 4) << 4) | (inp.SBIT & 0xF);
//                     {7'b0,SBIT[4],~SBIT[4],SBIT[3:0]};
        } else if ((EN != 0) & (old.DONE == 0) & (old.CNTS == 0) & (old.CNTM == 0) & (old.CNTP == 1)) {
            XS = (int) ((old.RESULT >> 5) & 0x1FFF);//	RESULT[17:5];
        } else if ((EN != 0) & (old.DONE == 0)) {
            XS = (bit(old.XS, 0) << 12) | (old.XS >> 1);//	{XS[0],XS[12:1]};
        } else {
            XS = old.XS;
        }

        if (EN == 0) {	//casex ({EN,DONE,ENshift_trig4})
            RESULTn_1 = 0;
            RESULTn_2 = 0;
        } else if ((EN != 0) && (old.DONE == 0)) {
            if ((old.CNTM == 0) & (old.CNTS == 0)) {
                RESULTn_1 = 0;
                RESULTn_2 = 0;
            } else if ((EN != 0) & (old.DONE == 0) & (old.CNTM >= old.CNTD) & (old.CNTS == 0)) {
                RESULTn_1 = bit(old.RESULT, 0);
                RESULTn_2 = old.RESULTn_1;
            } else if ((EN != 0) & (old.DONE == 0) & (old.CNTM == 0) & (old.CNTS == 0) & (old.CNTP == 6)) {
                RESULTn_1 = 0;
                RESULTn_2 = 0;
            } else {
                RESULTn_1 = old.RESULTn_1;
                RESULTn_2 = old.RESULTn_2;
            }
        } else if ((EN != 0) & (old.DONE != 0) & (old.ENshift_trig4 != 0)) {
            RESULTn_1 = bit(old.RESULT, 0);
            RESULTn_2 = old.RESULTn_1;
        } else {
            RESULTn_1 = old.RESULTn_1;
            RESULTn_2 = old.RESULTn_2;
        }

        if (EN == 0) { //casex( {EN,DONE,ENshift_trig4})
            RESULT = 0b11111_1111111111_1111111111_1111111111L;
        } else if ((EN != 0) && (old.DONE == 0)) {
            if (old.CNTS == 0 && !(old.CNTM == 0 && old.CNTP == 1)) {
                RESULT = (((long) S2) << 34) | (old.RESULT >> 1);
            } else if (old.CNTS == 0 && old.CNTM == 0 && old.CNTP == 1) {
                RESULT = inp.K5BIT;
            } else {
                RESULT = old.RESULT;
            }
        } else if ((EN != 0) && (old.DONE != 0) && (old.ENshift_trig4 != 0)) {
            RESULT = (old.RESULT & ~0xFFF) | 0x800 | ((old.RESULT >> 1) & 0x7FF);
        } else {
            RESULT = old.RESULT;
        }

        if (EN == 0) {
            WORK = inp.T;
        } else if (EN != 0 && old.DONE == 0) {
            if (old.CNTS != 0) {
                WORK = (S << 12) | (old.WORK >> 1);
            } else if (old.CNTS == 0 && old.CNTM != 0) {
                WORK = (S << 12) | (S << 11) | (old.WORK >> 2);
            } else {
                WORK = 0;
            }
        } else {
            WORK = old.WORK;
        }

//===================================RESULTs==============================================================;
        int RESULTshift = ((old.ENshift_trig4 != 0) & (old.DONE != 0)) ? old.RESULTn_2 : 1;

        if (old.DONE != 0) {
            RESULTout = bit(RESULT, 34) != 0 ? 0x000 : (old.RESULT & 0x3FFFFF000L) != 0 ? 0xFFF : (int) old.RESULT & 0xFFF;
        } else {
            RESULTout = old.RESULTout;
        }

    }

    /**
     * This should be a composition of the negedge and posedge the method is not
     * used and maybe incorrect
     *
     * @param inp input
     * @param old previous state
     */
    void clk(Inp inp, PolyState old) {
        if (inp.RST != 0) {
            ENshift_trig0 = 0;
        } else {
            ENshift_trig0 = inp.ENshift;
        }
        if (inp.RST != 0) {
            ENshift_trig1 = 0;
        } else {
            ENshift_trig1 = old.ENshift_trig0;
        }
        if (inp.RST != 0) {
            ENshift_trig2 = 0;
        } else {
            ENshift_trig2 = old.ENshift_trig1;
        }
        if (inp.RST != 0) {
            ENshift_trig3 = 0;
        } else {
            ENshift_trig3 = old.ENshift_trig2;
        }
        if (inp.RST != 0) {
            ENshift_trig4 = 0;
        } else {
            ENshift_trig4 = old.ENshift_trig3;
        }

        if (inp.RST != 0) {
            ENwork_trig0 = 0;
        } else {
            ENwork_trig0 = inp.ENwork;
        }
        if (inp.RST != 0) {
            ENwork_trig1 = 0;
        } else {
            ENwork_trig1 = old.ENwork_trig0;
        }

        int PAUSE = old.ENshift_trig1 & ~old.ENshift_trig4;

        int EN = (old.ENwork_trig1 | old.ENshift_trig4) & ~PAUSE;

        if (inp.RST != 0) {
            EN_trig = 0;
        } else {
            EN_trig = EN;
        }

        //====================COUNTERS===========================;
        if (inp.RST != 0) {
            CNTP = 0;
        } else if ((old.EN_trig != 0) & (old.CNTS == 0) & (old.CNTM == 0)) {
            CNTP = old.CNTP + 1;
        } else if (old.EN_trig == 0) {
            CNTP = 0;
        } else {
            CNTP = old.CNTP;
        }

        if (inp.RST != 0) {
            DONE = 0;
        } else if ((old.EN_trig != 0) & (old.CNTS == 0) & (old.CNTM == 0)) {
            DONE = (old.CNTP == 6) ? 1 : 0;
        } else if (old.EN_trig == 0) {
            DONE = 0;
        } else {
            DONE = old.DONE;
        }

        if (inp.RST != 0) {
            CNTS = 12;
        } else if (old.EN_trig == 0) {
            CNTS = 12;
        } else if ((old.EN_trig != 0) & (old.DONE == 0) & (old.CNTS == 0)) {
            CNTS = 12;
        } else if ((old.EN_trig != 0) & (old.DONE == 0)) {
            CNTS = old.CNTS - 1;
        } else {
            CNTS = old.CNTS;
        }

        if (inp.RST != 0) {
            CNTM = 34;
            CNTD = 0;
        } else if (old.EN_trig == 0) {
            CNTM = 34;
            CNTD = 0;
        } else if ((old.EN_trig != 0) & (old.DONE == 0) & (old.CNTS == 0) & (old.CNTM != 0)) {
            CNTM = old.CNTM - 1;
            CNTD = old.CNTD;
        } else if ((old.EN_trig != 0) & (old.DONE == 0) & (old.CNTS == 0) & (old.CNTM == 0)) {
            switch (CNTP) {
                case 0:
                    CNTM = 34 + 1;
                    CNTD = 0;
                    break; //+1 bacause xs latch data one takt later;
                case 1:
                    CNTM = 34;
                    CNTD = 0;
                    break; //Z0 //1:begin CNTM  <=	34;	CNTD <= 0; end
                case 2:
                    CNTM = 34 + 10;
                    CNTD = 10;
                    break;	//Z1
                case 3:
                    CNTM = 34 + 10;
                    CNTD = 10;
                    break;	//Z2
                case 4:
                    CNTM = 34 + 10;
                    CNTD = 10;
                    break; //Z3
                case 5:
                    CNTM = 34 + 14;
                    CNTD = 14;
                    break; //Z41
                case 6:
                    CNTM = 0;
                    CNTD = 0;
                    break; //Z4
                default:
                    throw new AssertionError();
            }
        } else {
            CNTM = old.CNTM;
            CNTD = old.CNTD;
        }

        //=====================MULTIPLIER===========================;
        int m = (old.CNTM >= old.CNTD) ? (bit(old.RESULT, 0) & ~old.RESULTn_1) : (old.RESULTn_1 & ~old.RESULTn_1);
        int p = (old.CNTM >= old.CNTD) ? (~bit(old.RESULT, 0) & old.RESULTn_1) : (~old.RESULTn_1 & old.RESULTn_2);

        int S1in = m != 0 ? bit(~old.XS, 0) : (p != 0 ? bit(old.XS, 0) : 0);
        int S2in = bit(old.WORK, 0);
        int Pin = (old.CNTS == 12) ? m : old.P;

        int S = S1in ^ S2in ^ Pin;
        if (inp.RST != 0) {
            P = 0;
        } else if ((EN != 0) & (old.DONE == 0)) {
            P = S1in & S2in & ~Pin | S1in & ~S2in & Pin | ~S1in & S2in & Pin | S1in & S2in & Pin;
        } else if (EN == 0) {
            P = 0;
        } else {
            P = old.P;
        }
        System.out.println(" m=" + m + " p=" + p + " S1in=" + S1in + " S2in=" + S2in + " Pin=" + Pin + " S=" + S + " P=" + P);

        //====================SUMMATOR1===============================;
        int SL;
        switch (CNTP) {
            case 0:
                SL = 0;
                break;
            case 1:
                switch (CNTM) {
                    case 31:
                        SL = 1;
                        break;
                    default:
                        SL = 0;
                }
                break;
            case 2:
                switch (CNTM) {
                    case 35:
                        SL = 1;
                        break;
                    case 34 - 9:
                        SL = bit(inp.K4BIT, 0);
                        break; //BIT #0:  (RES_Size -1)(34) + REZ_shift(0) - Kshift(9));
                    case 33 - 9:
                        SL = bit(inp.K4BIT, 1);
                        break;
                    case 32 - 9:
                        SL = bit(inp.K4BIT, 2);
                        break;
                    case 31 - 9:
                        SL = bit(inp.K4BIT, 3);
                        break;
                    case 30 - 9:
                        SL = bit(inp.K4BIT, 4);
                        break;
                    default:
                        SL = 0;
                }
                break;
            case 3:
                switch (CNTM) {
                    case 35:
                        SL = 1;
                        break;
                    case 34 + 10 - 15 - 4:
                        SL = bit(inp.K3BIT, 0);
                        break;//BIT #0:  (RES_Size -1)(34) + REZ_shift(10) - Kshift(15) - fraction(4);
                    case 33 + 10 - 15 - 4:
                        SL = bit(inp.K3BIT, 1);
                        break;
                    case 32 + 10 - 15 - 4:
                        SL = bit(inp.K3BIT, 2);
                        break;
                    case 31 + 10 - 15 - 4:
                        SL = bit(inp.K3BIT, 3);
                        break;
                    case 30 + 10 - 15 - 4:
                        SL = bit(inp.K3BIT, 4);
                        break;
                    default:
                        SL = 0;
                }
                break;
            case 4:
                switch (CNTM) {
                    case 35:
                        SL = 1;
                        break;
                    case 34 + 10 - 13 - 4:
                        SL = bit(inp.K2BIT, 0);
                        break;//BIT #0:  (RES_Size -1)(34) + REZ_shift(10) - Kshift(13) - fraction(4);
                    case 33 + 10 - 13 - 4:
                        SL = bit(inp.K2BIT, 1);
                        break;
                    case 32 + 10 - 13 - 4:
                        SL = bit(inp.K2BIT, 2);
                        break;
                    case 31 + 10 - 13 - 4:
                        SL = bit(inp.K2BIT, 3);
                        break;
                    case 30 + 10 - 13 - 4:
                        SL = bit(inp.K2BIT, 4);
                        break;
                    case 29 + 10 - 13 - 4:
                        SL = bit(inp.K2BIT, 5);
                        break;
                    case 28 + 10 - 13 - 4:
                        SL = bit(inp.K2BIT, 6);
                        break;
                    default:
                        SL = 0;
                }
                break;
            case 5:
                switch (CNTM) {
                    case 34 + 10 - 13 - 4:
                        SL = bit(~inp.K1BIT, 0);
                        break;//BIT #0:  (RES_Size -1)(34) + REZ_shift(10) - Kshift(13) - fraction(4);
                    case 33 + 10 - 13 - 4:
                        SL = bit(~inp.K1BIT, 1);
                        break;
                    case 32 + 10 - 13 - 4:
                        SL = bit(~inp.K1BIT, 2);
                        break;
                    case 31 + 10 - 13 - 4:
                        SL = bit(~inp.K1BIT, 3);
                        break;
                    case 30 + 10 - 13 - 4:
                        SL = bit(~inp.K1BIT, 4);
                        break;
                    case 29 + 10 - 13 - 4:
                        SL = bit(~inp.K1BIT, 5);
                        break;
                    case 28 + 10 - 13 - 4:
                        SL = bit(~inp.K1BIT, 6);
                        break;
                    case 27 + 10 - 13 - 4:
                        SL = bit(~inp.K1BIT, 7);
                        break;
                    default:
                        SL = 1;
                }
                break;
            case 6:
                switch (CNTM) {
                    case 35:
                        SL = 1;
                        break;
                    default:
                        SL = 0;
                }
                break;
            default:
                SL = 0;
        }

        int S1 = bit(old.WORK, 1) ^ SL ^ old.P1;

        if (inp.RST != 0) {
            P1 = 0;
        } else if (EN == 0) {
            P1 = 0;
        } else if ((EN != 0) & (old.DONE == 0) & (old.CNTS == 0)) {
            P1 = bit(old.WORK, 1) & SL & ~old.P1 | bit(old.WORK, 1) & ~SL & old.P1 | bit(~old.WORK, 1) & SL & old.P1 | bit(old.WORK, 1) & SL & old.P1;
        } else if ((EN != 0) & (old.DONE == 0) & (old.CNTS == 0) & (old.CNTM == 0)) {
            P1 = 0;
        } else {
            P1 = old.P1;
        }
//        System.out.println(" SL="+SL+" WORK[1]="+bit(old.WORK,1)+" P1="+old.P1+" S1="+S1+" P1="+P1);

        //====================SUMMATOR2===============================;
        int SL2;
        switch (old.CNTP) {
            case 0:
                switch (old.CNTM) {
                    case 33:
                    case 32:
                    case 31:
                    case 30:
                    case 29:
                    case 28:
                    case 27:
                    case 26:
                    case 24:
                        SL2 = 0;
                        break;
                    default:
                        SL2 = 1;
                }
                break;
            case 2:
                switch (old.CNTM) {
                    case 34:
                    case 33:
                    case 32:
                    case 31:
                    case 30:
                    case 29:
                    case 28:
                    case 27:
                    case 26:
                    case 22:
                    case 21:
                        SL2 = 0;
                        break;
                    default:
                        SL2 = 1;
                }
                break;
            case 3:
                switch (old.CNTM) {
                    case 34 + 10 - 18 - 4:
                        SL2 = 1;
                        break;//..0010_0000_0000_0000_0000
                    default:
                        SL2 = 0;
                }
                break;
            case 4:
                switch (old.CNTM) {
                    case 34 + 10 - 17 - 4:
                    case 34 + 10 - 15 - 4:
                        SL2 = 1;
                        break;//..0000_0010_1000_0000_0000_0000
                    default:
                        SL2 = 0;
                }
                break;
            case 5:
                switch (old.CNTM) {
                    case 34 + 10:
                    case 33 + 10:
                    case 32 + 10:
                    case 31 + 10:
                    case 34 + 10 - 4:
                    case 33 + 10 - 4:
                    case 32 + 10 - 4:
                    case 31 + 10 - 4:
                    case 30 + 10 - 4:
                    case 29 + 10 - 4:
                    case 28 + 10 - 4:
                    case 27 + 10 - 4:
                    case 26 + 10 - 4:
                    case 25 + 10 - 4:
                    case 24 + 10 - 4:
                    case 23 + 10 - 4:
                    case 21 + 10 - 4:
                    case 16 + 10 - 4:
                    case 15 + 10 - 4:
                        SL2 = 0;
                        break;//..11_0011_1101_0000_0000_0000
                    default:
                        SL2 = 1;
                }
                break;
            case 6:
                switch (old.CNTM) {
                    case 34 + 14 - 20 - 4:
                    case 34 + 14 - 13 - 4:
                        SL2 = 1;
                        break;//..0001_0000_0010_0000_0000_0000//1032
                    default:
                        SL2 = 0;
                }
                break;
            default:
                SL2 = 0;
        }

        int S2 = S1 ^ SL2 ^ old.P2;

        if (inp.RST != 0) {
            P2 = 0;
        } else if (EN == 0) {
            P2 = 0;
        } else if ((EN != 0) & (old.DONE == 0) & (old.CNTS == 0)) {
            P2 = S1 & SL2 & ~old.P2 | S1 & ~SL2 & old.P2 | ~S1 & SL2 & old.P2 | S1 & SL2 & old.P2;
        } else if ((EN != 0) & (old.DONE == 0) & (old.CNTS == 0) & (old.CNTM == 0)) {
            P2 = 0;
        } else {
            P2 = old.P2;
        }
//        System.out.println(" SL2="+SL2+" S1="+S1+" P2="+old.P2+" S2="+S2+" P2="+P2);

        //====================SHIFT REGISTERS========================;
        if (inp.RST != 0) {
            XS = 0b0000000000010;
        } else if (EN == 0) {
            XS = inp.INF << 3;
        } else if ((EN != 0) & (old.DONE == 0) & (old.CNTS == 0) & (old.CNTM == 0) & (old.CNTP == 0)) {
            XS = (bit(inp.SBIT, 4) << 5) | (bit(~inp.SBIT, 4) << 4) | (inp.SBIT & 0xF);
//                     {7'b0,SBIT[4],~SBIT[4],SBIT[3:0]};
        } else if ((EN != 0) & (old.DONE == 0) & (old.CNTS == 0) & (old.CNTM == 0) & (old.CNTP == 1)) {
            XS = (int) ((old.RESULT >> 5) & 0x1FFF);//	RESULT[17:5];
        } else if ((EN != 0) & (old.DONE == 0)) {
            XS = (bit(old.XS, 0) << 12) | (old.XS >> 1);//	{XS[0],XS[12:1]};
        } else {
            XS = old.XS;
        }

        if (inp.RST != 0) {
            RESULTn_1 = 0;
            RESULTn_2 = 0;
        } else if (EN == 0) {	//casex ({EN,DONE,ENshift_trig4})
            RESULTn_1 = 0;
            RESULTn_2 = 0;
        } else if ((EN != 0) && (old.DONE == 0)) {
            if ((old.CNTM == 0) & (old.CNTS == 0)) {
                RESULTn_1 = 0;
                RESULTn_2 = 0;
            } else if ((EN != 0) & (old.DONE == 0) & (old.CNTM >= old.CNTD) & (old.CNTS == 0)) {
                RESULTn_1 = bit(old.RESULT, 0);
                RESULTn_2 = old.RESULTn_1;
            } else if ((EN != 0) & (old.DONE == 0) & (old.CNTM == 0) & (old.CNTS == 0) & (old.CNTP == 6)) {
                RESULTn_1 = 0;
                RESULTn_2 = 0;
            } else {
                RESULTn_1 = old.RESULTn_1;
                RESULTn_2 = old.RESULTn_2;
            }
        } else if ((EN != 0) & (old.DONE != 0) & (old.ENshift_trig4 != 0)) {
            RESULTn_1 = bit(old.RESULT, 0);
            RESULTn_2 = old.RESULTn_1;
        } else {
            RESULTn_1 = old.RESULTn_1;
            RESULTn_2 = old.RESULTn_2;
        }

        if (inp.RST != 0) {
            RESULT = 0b11111_1111111111_1111111111_1111111111L;
        } else if (EN == 0) { //casex( {EN,DONE,ENshift_trig4})
            RESULT = 0b11111_1111111111_1111111111_1111111111L;
        } else if ((EN != 0) && (old.DONE == 0)) {
            if (old.CNTS == 0 && !(old.CNTM == 0 && old.CNTP == 1)) {
                RESULT = (S2 << 34) | (old.RESULT >> 1);
            } else if (old.CNTS == 0 && old.CNTM == 0 && old.CNTP == 1) {
                RESULT = inp.K5BIT;
            } else {
                RESULT = old.RESULT;
            }
        } else if ((EN != 0) && (old.DONE != 0) && (old.ENshift_trig4 != 0)) {
            RESULT = (old.RESULT & ~0xFFF) | 0x800 | ((old.RESULT >> 1) & 0x7FF);
        } else {
            RESULT = old.RESULT;
        }

        if (inp.RST != 0) {
            WORK = 0;
        } else if (EN == 0) {
            WORK = inp.T;
        } else if (EN != 0 && old.DONE == 0) {
            if (old.CNTS != 0) {
                WORK = (S << 12) | (old.WORK >> 1);
            } else if (old.CNTS == 0 && old.CNTM != 0) {
                WORK = (S << 12) | (S << 11) | (old.WORK >> 2);
            } else {
                WORK = 0;
            }
        } else {
            WORK = old.WORK;
        }

//===================================RESULTs==============================================================;
        int RESULTshift = ((old.ENshift_trig4 != 0) & (old.DONE != 0)) ? old.RESULTn_2 : 1;

        if (inp.RST != 0) {
            RESULTout = 0;
        } else if (old.DONE != 0) {
            RESULTout = bit(RESULT, 34) != 0 ? 0x000 : (old.RESULT & 0x3FFFFF000L) != 0 ? 0xFFF : (int) old.RESULT & 0xFFF;
        } else {
            RESULTout = old.RESULTout;
        }

    }

    /**
     * Extract certain bit from long value
     *
     * @param v long value
     * @param b bit number
     * @return
     */
    static int bit(long v, int b) {
        return (int) ((v >> b) & 1);
    }

    /**
     * Assign the state from other state
     *
     * @param that
     */
    void assign(PolyState that) {
        this.RESULTout = that.RESULTout;
        this.XS = that.XS;
        this.RESULT = that.RESULT;
        this.WORK = that.WORK;
        this.P = that.P;
//        this.SL = that.SL;
        this.P1 = that.P1;
//        this.SL2 = that.SL2;
        this.P2 = that.P2;
        this.RESULTn_1 = that.RESULTn_1;
        this.RESULTn_2 = that.RESULTn_2;
        this.CNTP = that.CNTP;
        this.CNTS = that.CNTS;
        this.CNTM = that.CNTM;
        this.CNTD = that.CNTD;
        this.DONE = that.DONE;
        this.ENwork_trig0 = that.ENwork_trig0;
        this.ENwork_trig1 = that.ENwork_trig1;
        this.ENshift_trig0 = that.ENshift_trig0;
        this.ENshift_trig1 = that.ENshift_trig1;
        this.ENshift_trig2 = that.ENshift_trig2;
        this.ENshift_trig3 = that.ENshift_trig3;
        this.ENshift_trig4 = that.ENshift_trig4;
        this.EN_trig = that.EN_trig;
    }
}
