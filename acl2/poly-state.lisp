(in-package "ACL2")
(include-book "unsigned-p")
(include-book "std/basic/arith-equiv-defs" :dir :system)

(local (include-book "centaur/bitops/ihsext-basics" :dir :system))

(fty::defprod
 poly-in
 ((|CLK|           unsigned-1-p)
  (|RST|           unsigned-1-p)
  (|K1BIT|         unsigned-8-p)
  (|K2BIT|         unsigned-7-p)
  (|K3BIT|         unsigned-5-p)
  (|K4BIT|         unsigned-5-p)
  (|K5BIT|         unsigned-4-p)
  (|SBIT|          unsigned-5-p)
  (|INF|           unsigned-6-p)
  (|TT|            unsigned-12-p)
  (|ENwork|        unsigned-1-p)
  (|ENshift|       unsigned-1-p)
  ))

(fty::defprod
 poly-st
 ((|RESULTout|     unsigned-12-p)
  (|XS|            unsigned-13-p)
  (|RESULT|        unsigned-35-p)
  (|WORK|          unsigned-13-p)
  (|P|             unsigned-1-p)
  (|P1|            unsigned-1-p)
  (|P2|            unsigned-1-p)
  (|RESULTn_1|     unsigned-1-p)
  (|RESULTn_2|     unsigned-1-p)
  (|CNTP|          unsigned-3-p)
  (|CNTS|          unsigned-4-p)
  (|CNTM|          unsigned-6-p)
  (|CNTD|          unsigned-6-p)
  (|DONE|          unsigned-1-p)
  (|ENwork_trig0|  unsigned-1-p)
  (|ENwork_trig1|  unsigned-1-p)
  (|ENshift_trig0| unsigned-1-p)
  (|ENshift_trig1| unsigned-1-p)
  (|ENshift_trig2| unsigned-1-p)
  (|ENshift_trig3| unsigned-1-p)
  (|ENshift_trig4| unsigned-1-p)
  (|EN_trig|       unsigned-1-p)))

(local
 (defrule bitp-unsigned-1-p
   (implies (unsigned-1-p x)
            (bitp x))))

(defrule |POLY-IN->CLK-TYPE|
  (bitp (|POLY-IN->CLK| in))
  :rule-classes :type-prescription
  :cases ((unsigned-1-p (|POLY-IN->CLK| in))))

(defrule |POLY-IN->RST-TYPE|
  (bitp (|POLY-IN->RST| in))
  :rule-classes :type-prescription
  :cases ((unsigned-1-p (|POLY-IN->RST| in))))

(defrule |POLY-IN->K1BIT-TYPE|
  (natp (|POLY-IN->K1BIT| in))
  :rule-classes :type-prescription
  :cases ((unsigned-8-p (|POLY-IN->K1BIT| in))))

(defrule |POLY-IN->K2BIT-TYPE|
  (natp (|POLY-IN->K2BIT| in))
  :rule-classes :type-prescription
  :cases ((unsigned-7-p (|POLY-IN->K2BIT| in))))

(defrule |POLY-IN->K3BIT-TYPE|
  (natp (|POLY-IN->K3BIT| in))
  :rule-classes :type-prescription
  :cases ((unsigned-5-p (|POLY-IN->K3BIT| in))))

(defrule |POLY-IN->K4BIT-TYPE|
  (natp (|POLY-IN->K4BIT| in))
  :rule-classes :type-prescription
  :cases ((unsigned-5-p (|POLY-IN->K4BIT| in))))

(defrule |POLY-IN->K5BIT-TYPE|
  (natp (|POLY-IN->K5BIT| in))
  :rule-classes :type-prescription
  :cases ((unsigned-4-p (|POLY-IN->K5BIT| in))))

(defrule |POLY-IN->SBIT-TYPE|
  (natp (|POLY-IN->SBIT| in))
  :rule-classes :type-prescription
  :cases ((unsigned-5-p (|POLY-IN->SBIT| in))))

(defrule |POLY-IN->INF-TYPE|
  (natp (|POLY-IN->INF| in))
  :rule-classes :type-prescription
  :cases ((unsigned-6-p (|POLY-IN->INF| in))))

(defrule |POLY-IN->TT-TYPE|
  (natp (|POLY-IN->TT| in))
  :rule-classes :type-prescription
  :cases ((unsigned-12-p (|POLY-IN->TT| in))))

(defrule |POLY-IN->ENwork-TYPE|
  (bitp (|POLY-IN->ENwork| in))
  :rule-classes :type-prescription
  :cases ((unsigned-1-p (|POLY-IN->ENwork| in))))

(defrule |POLY-IN->ENshift-TYPE|
  (bitp (|POLY-IN->ENshift| in))
  :rule-classes :type-prescription
  :cases ((unsigned-1-p (|POLY-IN->ENshift| in))))

;;;;

(defrule |POLY-ST->RESULTout-TYPE|
  (natp (|POLY-ST->RESULTout| st))
  :rule-classes :type-prescription
  :cases ((unsigned-12-p (|POLY-ST->RESULTout| st))))

(defrule |POLY-ST->XS-TYPE|
  (natp (|POLY-ST->XS| st))
  :rule-classes :type-prescription
  :cases ((unsigned-13-p (|POLY-ST->XS| st))))

(defrule |POLY-ST->RESULT-TYPE|
  (natp (|POLY-ST->RESULT| st))
  :rule-classes :type-prescription
  :cases ((unsigned-35-p (|POLY-ST->RESULT| st))))

(defrule |POLY-ST->WORK-TYPE|
  (natp (|POLY-ST->WORK| st))
  :rule-classes :type-prescription
  :cases ((unsigned-13-p (|POLY-ST->WORK| st))))

(defrule |POLY-ST->P-TYPE|
  (bitp (|POLY-ST->P| st))
  :rule-classes :type-prescription
  :cases ((unsigned-1-p (|POLY-ST->P| st))))

(defrule |POLY-ST->P1-TYPE|
  (bitp (|POLY-ST->P1| st))
  :rule-classes :type-prescription
  :cases ((unsigned-1-p (|POLY-ST->P1| st))))

(defrule |POLY-ST->P2-TYPE|
  (bitp (|POLY-ST->P2| st))
  :rule-classes :type-prescription
  :cases ((unsigned-1-p (|POLY-ST->P2| st))))

(defrule |POLY-ST->RESULTn_1-TYPE|
  (bitp (|POLY-ST->RESULTn_1| st))
  :rule-classes :type-prescription
  :cases ((unsigned-1-p (|POLY-ST->RESULTn_1| st))))

(defrule |POLY-ST->RESULTn_2-TYPE|
  (bitp (|POLY-ST->RESULTn_2| st))
  :rule-classes :type-prescription
  :cases ((unsigned-1-p (|POLY-ST->RESULTn_2| st))))

(defrule |POLY-ST->CNTP-TYPE|
  (natp (|POLY-ST->CNTP| st))
  :rule-classes :type-prescription
  :cases ((unsigned-3-p (|POLY-ST->CNTP| st))))

(defrule |POLY-ST->CNTS-TYPE|
  (natp (|POLY-ST->CNTS| st))
  :rule-classes :type-prescription
  :cases ((unsigned-4-p (|POLY-ST->CNTS| st))))

(defrule |POLY-ST->CNTM-TYPE|
  (natp (|POLY-ST->CNTM| st))
  :rule-classes :type-prescription
  :cases ((unsigned-6-p (|POLY-ST->CNTM| st))))

(defrule |POLY-ST->CNTD-TYPE|
  (natp (|POLY-ST->CNTD| st))
  :rule-classes :type-prescription
  :cases ((unsigned-6-p (|POLY-ST->CNTD| st))))

(defrule |POLY-ST->DONE-TYPE|
  (bitp (|POLY-ST->DONE| st))
  :rule-classes :type-prescription
  :cases ((unsigned-1-p (|POLY-ST->DONE| st))))

(defrule |POLY-ST->ENwork_trig0-TYPE|
  (bitp (|POLY-ST->ENwork_trig0| st))
  :rule-classes :type-prescription
  :cases ((unsigned-1-p (|POLY-ST->ENwork_trig0| st))))

(defrule |POLY-ST->ENwork_trig1-TYPE|
  (bitp (|POLY-ST->ENwork_trig1| st))
  :rule-classes :type-prescription
  :cases ((unsigned-1-p (|POLY-ST->ENwork_trig1| st))))

(defrule |POLY-ST->ENshift_trig0-TYPE|
  (bitp (|POLY-ST->ENshift_trig0| st))
  :rule-classes :type-prescription
  :cases ((unsigned-1-p (|POLY-ST->ENshift_trig0| st))))

(defrule |POLY-ST->ENshift_trig1-TYPE|
  (bitp (|POLY-ST->ENshift_trig1| st))
  :rule-classes :type-prescription
  :cases ((unsigned-1-p (|POLY-ST->ENshift_trig1| st))))

(defrule |POLY-ST->ENshift_trig2-TYPE|
  (bitp (|POLY-ST->ENshift_trig2| st))
  :rule-classes :type-prescription
  :cases ((unsigned-1-p (|POLY-ST->ENshift_trig2| st))))

(defrule |POLY-ST->ENshift_trig3-TYPE|
  (bitp (|POLY-ST->ENshift_trig3| st))
  :rule-classes :type-prescription
  :cases ((unsigned-1-p (|POLY-ST->ENshift_trig3| st))))

(defrule |POLY-ST->ENshift_trig4-TYPE|
  (bitp (|POLY-ST->ENshift_trig4| st))
  :rule-classes :type-prescription
  :cases ((unsigned-1-p (|POLY-ST->ENshift_trig4| st))))

(defrule |POLY-ST->EN_trig-TYPE|
  (bitp (|POLY-ST->EN_trig| st))
  :rule-classes :type-prescription
  :cases ((unsigned-1-p (|POLY-ST->EN_trig| st))))

;;;;

(define reset
  ()
  :returns (st poly-st-p)
  (make-poly-st
   :|RESULTout|      0
   :|XS|             #b0000000000010
   :|RESULT|         #ub11111_1111111111_1111111111_1111111111
   :|WORK|           0
   :|P|              0
   :|P1|             0
   :|P2|             0
   :|RESULTn_1|      0
   :|RESULTn_2|      0
   :|CNTP|           0
   :|CNTS|           12
   :|CNTM|           34
   :|CNTD|           0
   :|DONE|           0
   :|ENwork_trig0|   0
   :|ENwork_trig1|   0
   :|ENshift_trig0|  0
   :|ENshift_trig1|  0
   :|ENshift_trig2|  0
   :|ENshift_trig3|  0
   :|ENshift_trig4|  0
   :|EN_trig|        0))

(local (in-theory (disable loghead unsigned-byte-p)))

(define |NEXT-CNTP|
  ((st poly-st-p))
  :returns (CNTP unsigned-3-p)
  (cond ((and (not (= (|POLY-ST->EN_trig| st) 0))
              (= (|POLY-ST->CNTS| st) 0)
              (= (|POLY-ST->CNTM| st) 0))
         (loghead 3 (1+ (|POLY-ST->CNTP| st))))
        ((= (|POLY-ST->EN_trig| st) 0)
         0)
        (t (|POLY-ST->CNTP| st))))

(define |NEXT-CNTS|
  ((st poly-st-p))
  :returns (CNTS unsigned-4-p)
  (cond ((= (|POLY-ST->EN_trig| st) 0)
         12)
        ((and (not (= (|POLY-ST->EN_trig| st) 0))
              (= (|POLY-ST->DONE| st) 0)
              (= (|POLY-ST->CNTS| st) 0))
         12)
        ((and (not (= (|POLY-ST->EN_trig| st) 0))
              (= (|POLY-ST->DONE| st) 0))
         (loghead 4 (1- (|POLY-ST->CNTS| st))))
        (t (|POLY-ST->CNTS| st))))

(define |NEXT-CNTM|
  ((st poly-st-p))
  :returns (CNTM unsigned-6-p)
  (cond ((= (|POLY-ST->EN_trig| st) 0)
         34)
        ((and (not (= (|POLY-ST->EN_trig| st) 0))
              (= (|POLY-ST->DONE| st) 0)
              (= (|POLY-ST->CNTS| st) 0)
              (not (= (|POLY-ST->CNTM| st) 0)))
         (loghead 6 (1- (|POLY-ST->CNTM| st))))
        ((and (not (= (|POLY-ST->EN_trig| st) 0))
              (= (|POLY-ST->DONE| st) 0)
              (= (|POLY-ST->CNTS| st) 0)
              (= (|POLY-ST->CNTM| st) 0))
         (cond ((= (|POLY-ST->CNTP| st) 0) (+ 34 1))
               ((= (|POLY-ST->CNTP| st) 1) 34)
               ((= (|POLY-ST->CNTP| st) 2) (+ 34 10))
               ((= (|POLY-ST->CNTP| st) 3) (+ 34 10))
               ((= (|POLY-ST->CNTP| st) 4) (+ 34 10))
               ((= (|POLY-ST->CNTP| st) 5) (+ 34 14))
               ((= (|POLY-ST->CNTP| st) 6) 0)
               (t (|POLY-ST->CNTM| st))))
        (t (|POLY-ST->CNTM| st))))

(define |NEXT-CNTD|
  ((st poly-st-p))
  :returns (CNTD unsigned-6-p)
  (cond ((= (|POLY-ST->EN_trig| st) 0)
         0)
        ((and (not (= (|POLY-ST->EN_trig| st) 0))
              (= (|POLY-ST->DONE| st) 0)
              (= (|POLY-ST->CNTS| st) 0)
              (not (= (|POLY-ST->CNTM| st) 0)))
         (|POLY-ST->CNTD| st))
        ((and (not (= (|POLY-ST->EN_trig| st) 0))
              (= (|POLY-ST->DONE| st) 0)
              (= (|POLY-ST->CNTS| st) 0)
              (= (|POLY-ST->CNTM| st) 0))
         (cond ((= (|POLY-ST->CNTP| st) 0) 0)
               ((= (|POLY-ST->CNTP| st) 1) 0)
               ((= (|POLY-ST->CNTP| st) 2) 10)
               ((= (|POLY-ST->CNTP| st) 3) 10)
               ((= (|POLY-ST->CNTP| st) 4) 10)
               ((= (|POLY-ST->CNTP| st) 5) 14)
               ((= (|POLY-ST->CNTP| st) 6) 0)
               (t (|POLY-ST->CNTD| st))))
        (t (|POLY-ST->CNTD| st))))

(define |NEXT-DONE|
  ((st poly-st-p))
  :returns (DONE unsigned-1-p)
  (cond ((and (not (= (|POLY-ST->EN_trig| st) 0))
              (= (|POLY-ST->CNTS| st) 0)
              (= (|POLY-ST->CNTM| st) 0))
         (bool->bit (= (|POLY-ST->CNTP| st) 6)))
        ((= (|POLY-ST->EN_trig| st) 0)
         0)
        (t (|POLY-ST->DONE| st))))

(define negedge
  ((st poly-st-p))
  :returns (st poly-st-p)
  (make-poly-st
   :|RESULTout|      (|POLY-ST->RESULTout| st)
   :|XS|             (|POLY-ST->XS| st)
   :|RESULT|         (|POLY-ST->RESULT| st)
   :|WORK|           (|POLY-ST->WORK| st)
   :|P|              (|POLY-ST->P| st)
   :|P1|             (|POLY-ST->P1| st)
   :|P2|             (|POLY-ST->P2| st)
   :|RESULTn_1|      (|POLY-ST->RESULTn_1| st)
   :|RESULTn_2|      (|POLY-ST->RESULTn_2| st)
   :|CNTP|           (|NEXT-CNTP| st)
   :|CNTS|           (|NEXT-CNTS| st)
   :|CNTM|           (|NEXT-CNTM| st)
   :|CNTD|           (|NEXT-CNTD| st)
   :|DONE|           (|NEXT-DONE| st)
   :|ENwork_trig0|   (|POLY-ST->ENwork_trig0| st)
   :|ENwork_trig1|   (|POLY-ST->ENwork_trig1| st)
   :|ENshift_trig0|  (|POLY-ST->ENshift_trig0| st)
   :|ENshift_trig1|  (|POLY-ST->ENshift_trig1| st)
   :|ENshift_trig2|  (|POLY-ST->ENshift_trig2| st)
   :|ENshift_trig3|  (|POLY-ST->ENshift_trig3| st)
   :|ENshift_trig4|  (|POLY-ST->ENshift_trig4| st)
   :|EN_trig|        (|POLY-ST->EN_trig| st)))

(define |PAUSE|
  ((st poly-st-p))
  :returns (PAUSE unsigned-1-p)
  (bool->bit (and (not (= (|POLY-ST->ENshift_trig1| st) 0))
                  (= (|POLY-ST->ENshift_trig4| st) 0))))

(define |EN|
  ((st poly-st-p))
  :returns (EN unsigned-1-p)
  (bool->bit (and (or (not (= (|POLY-ST->ENwork_trig1| st) 0))
                      (not (= (|POLY-ST->ENshift_trig4| st) 0)))
                  (= (PAUSE st) 0))))

(define |m|
  ((st poly-st-p))
  :returns (EN unsigned-1-p)
  (bool->bit (if (>= (|POLY-ST->CNTM| st) (|POLY-ST->CNTD| st))
                 (and (logbitp 0 (|POLY-ST->RESULT| st))
                      (= (|POLY-ST->RESULTn_1| st) 0))
               (and (not (= (|POLY-ST->RESULTn_1| st) 0))
                    (= (|POLY-ST->RESULTn_1| st) 0)))))

(define |p|
  ((st poly-st-p))
  :returns (EN unsigned-1-p)
  (bool->bit (if (>= (|POLY-ST->CNTM| st) (|POLY-ST->CNTD| st))
                 (and (not (logbitp 0 (|POLY-ST->RESULT| st)))
                      (not (= (|POLY-ST->RESULTn_1| st) 0)))
               (and (= (|POLY-ST->RESULTn_1| st) 0)
                    (not (= (|POLY-ST->RESULTn_2| st) 0))))))

(define |S1in|
  ((st poly-st-p))
  :returns (S1in unsigned-1-p)
  (bool->bit (cond ((not (= (|m| st) 0)) (not (logbitp 0 (|POLY-ST->XS| st))))
                   ((not (= (|p| st) 0)) (logbitp 0 (|POLY-ST->XS| st)))
                   (t nil))))

(define |S2in|
  ((st poly-st-p))
  :returns (S1in unsigned-1-p)
  (logbit 0 (|POLY-ST->WORK| st)))

(define |Pin|
  ((st poly-st-p))
  :returns (Pin unsigned-1-p)
  (if (= (|POLY-ST->CNTS| st) 12)
      (|m| st)
    (|POLY-ST->P| st)))

(define |S_|
  ((st poly-st-p))
  :returns (Pin unsigned-1-p)
  (b-xor (|S1in| st) (b-xor (|S2in| st) (|Pin| st))))

(define |P-NEXT|
  ((st poly-st-p))
  :returns (P unsigned-1-p)
  (cond ((and (not (= (|EN| st) 0))
              (= (|POLY-ST->DONE| st) 0))
         (b-ior
          (b-and (|S1in| st) (b-and (|S2in| st) (b-not (|Pin| st))))
          (b-ior
           (b-and (|S1in| st) (b-and (b-not (|S2in| st)) (|Pin| st)))
           (b-ior
            (b-and (b-not (|S1in| st)) (b-and (|S2in| st) (|Pin| st)))
            (b-and (|S1in| st) (b-and (|S2in| st) (|Pin| st)))))))
        ((= (|EN| st) 0)
         0)
        (t (|POLY-ST->P| st))))

(define |SL|
  ((st poly-st-p)
   (in poly-in-p))
  :returns (SL unsigned-1-p)
  (cond
   ((= (|POLY-ST->CNTP| st) 0)
    0)
   ((= (|POLY-ST->CNTP| st) 1)
    (cond
     ((= (|POLY-ST->CNTM| st) 31) 1)
     (t 0)))
   ((= (|POLY-ST->CNTP| st) 2)
    (cond
     ((= (|POLY-ST->CNTM| st) 35) 1)
     ((= (|POLY-ST->CNTM| st) (- 34 9)) (logbit 0 (|POLY-IN->K4BIT| in)))
     ((= (|POLY-ST->CNTM| st) (- 33 9)) (logbit 1 (|POLY-IN->K4BIT| in)))
     ((= (|POLY-ST->CNTM| st) (- 32 9)) (logbit 2 (|POLY-IN->K4BIT| in)))
     ((= (|POLY-ST->CNTM| st) (- 31 9)) (logbit 3 (|POLY-IN->K4BIT| in)))
     ((= (|POLY-ST->CNTM| st) (- 30 9)) (logbit 4 (|POLY-IN->K4BIT| in)))
     (t 0)))
   ((= (|POLY-ST->CNTP| st) 3)
    (cond
     ((= (|POLY-ST->CNTM| st) 35) 1)
     ((= (|POLY-ST->CNTM| st) (+ 34 10 -15 -4)) (logbit 0 (|POLY-IN->K3BIT| in)))
     ((= (|POLY-ST->CNTM| st) (+ 33 10 -15 -4)) (logbit 1 (|POLY-IN->K3BIT| in)))
     ((= (|POLY-ST->CNTM| st) (+ 32 10 -15 -4)) (logbit 2 (|POLY-IN->K3BIT| in)))
     ((= (|POLY-ST->CNTM| st) (+ 31 10 -15 -4)) (logbit 3 (|POLY-IN->K3BIT| in)))
     ((= (|POLY-ST->CNTM| st) (+ 30 10 -15 -4)) (logbit 4 (|POLY-IN->K3BIT| in)))
     (t 0)))
   ((= (|POLY-ST->CNTP| st) 4)
    (cond
     ((= (|POLY-ST->CNTM| st) 35) 1)
     ((= (|POLY-ST->CNTM| st) (+ 34 10 -13 -4)) (logbit 0 (|POLY-IN->K2BIT| in)))
     ((= (|POLY-ST->CNTM| st) (+ 33 10 -13 -4)) (logbit 1 (|POLY-IN->K2BIT| in)))
     ((= (|POLY-ST->CNTM| st) (+ 32 10 -13 -4)) (logbit 2 (|POLY-IN->K2BIT| in)))
     ((= (|POLY-ST->CNTM| st) (+ 31 10 -13 -4)) (logbit 3 (|POLY-IN->K2BIT| in)))
     ((= (|POLY-ST->CNTM| st) (+ 30 10 -13 -4)) (logbit 4 (|POLY-IN->K2BIT| in)))
     ((= (|POLY-ST->CNTM| st) (+ 29 10 -13 -4)) (logbit 5 (|POLY-IN->K2BIT| in)))
     ((= (|POLY-ST->CNTM| st) (+ 28 10 -13 -4)) (logbit 6 (|POLY-IN->K2BIT| in)))
     (t 0)))
   ((= (|POLY-ST->CNTP| st) 5)
    (cond
     ((= (|POLY-ST->CNTM| st) (+ 34 10 -13 -4)) (b-not (logbit 0 (|POLY-IN->K1BIT| in))))
     ((= (|POLY-ST->CNTM| st) (+ 33 10 -13 -4)) (b-not (logbit 1 (|POLY-IN->K1BIT| in))))
     ((= (|POLY-ST->CNTM| st) (+ 32 10 -13 -4)) (b-not (logbit 2 (|POLY-IN->K1BIT| in))))
     ((= (|POLY-ST->CNTM| st) (+ 31 10 -13 -4)) (b-not (logbit 3 (|POLY-IN->K1BIT| in))))
     ((= (|POLY-ST->CNTM| st) (+ 30 10 -13 -4)) (b-not (logbit 4 (|POLY-IN->K1BIT| in))))
     ((= (|POLY-ST->CNTM| st) (+ 29 10 -13 -4)) (b-not (logbit 5 (|POLY-IN->K1BIT| in))))
     ((= (|POLY-ST->CNTM| st) (+ 28 10 -13 -4)) (b-not (logbit 6 (|POLY-IN->K1BIT| in))))
     ((= (|POLY-ST->CNTM| st) (+ 27 10 -13 -4)) (b-not (logbit 7 (|POLY-IN->K1BIT| in))))
     (t 1)))
   ((= (|POLY-ST->CNTP| st) 6)
    (cond
     ((= (|POLY-ST->CNTM| st) 35) 1)
     (t 0)))
   (t 0)))

(define |S1|
  ((st poly-st-p)
   (in poly-in-p))
  :returns (S1 unsigned-1-p)
  (b-xor
   (logbit 1 (|POLY-ST->WORK| st))
   (b-xor
    (|SL| st in)
    (|POLY-ST->P1| st))))

(define |P1-NEXT|
  ((st poly-st-p)
   (in poly-in-p)
   (fixbug booleanp))
  :returns (P1 unsigned-1-p)
  (if fixbug
      (cond ((= (|EN| st) 0)
             0)
            ((and (not (= (|EN| st) 0))
                  (= (|POLY-ST->DONE| st) 0)
                  (= (|POLY-ST->CNTS| st) 0)
                  (= (|POLY-ST->CNTM| st) 0))
             0)
            ((and (not (= (|EN| st) 0))
                  (= (|POLY-ST->DONE| st) 0)
                  (= (|POLY-ST->CNTS| st) 0))
             (b-ior
              (b-and (logbit 1 (|POLY-ST->WORK| st)) (b-and (|SL| st in) (b-not (|POLY-ST->P1| st))))
              (b-ior
               (b-and (logbit 1 (|POLY-ST->WORK| st)) (b-and (b-not (|SL| st in)) (|POLY-ST->P1| st)))
               (b-ior
                (b-and (b-not (logbit 1 (|POLY-ST->WORK| st))) (b-and (|SL| st in) (|POLY-ST->P1| st)))
                (b-and (logbit 1 (|POLY-ST->WORK| st)) (b-and (|SL| st in) (|POLY-ST->P1| st)))))))
            (t (|POLY-ST->P1| st)))
    (cond ((= (|EN| st) 0)
           0)
          ((and (not (= (|EN| st) 0))
                (= (|POLY-ST->DONE| st) 0)
                (= (|POLY-ST->CNTS| st) 0))
           (b-ior
            (b-and (logbit 1 (|POLY-ST->WORK| st)) (b-and (|SL| st in) (b-not (|POLY-ST->P1| st))))
            (b-ior
             (b-and (logbit 1 (|POLY-ST->WORK| st)) (b-and (b-not (|SL| st in)) (|POLY-ST->P1| st)))
             (b-ior
              (b-and (b-not (logbit 1 (|POLY-ST->WORK| st))) (b-and (|SL| st in) (|POLY-ST->P1| st)))
              (b-and (logbit 1 (|POLY-ST->WORK| st)) (b-and (|SL| st in) (|POLY-ST->P1| st)))))))
          ((and (not (= (|EN| st) 0))
                (= (|POLY-ST->DONE| st) 0)
                (= (|POLY-ST->CNTS| st) 0)
                (= (|POLY-ST->CNTM| st) 0))
           0)
          (t (|POLY-ST->P1| st)))))

(define |SL2|
  ((st poly-st-p))
  :returns (SL2 unsigned-1-p)
  (cond
   ((= (|POLY-ST->CNTP| st) 0)
    (if (or (= (|POLY-ST->CNTM| st) 33)
            (= (|POLY-ST->CNTM| st) 32)
            (= (|POLY-ST->CNTM| st) 31)
            (= (|POLY-ST->CNTM| st) 30)
            (= (|POLY-ST->CNTM| st) 29)
            (= (|POLY-ST->CNTM| st) 28)
            (= (|POLY-ST->CNTM| st) 27)
            (= (|POLY-ST->CNTM| st) 26)
            (= (|POLY-ST->CNTM| st) 24))
        0
      1))
   ((= (|POLY-ST->CNTP| st) 2)
    (if (or (= (|POLY-ST->CNTM| st) 34)
            (= (|POLY-ST->CNTM| st) 33)
            (= (|POLY-ST->CNTM| st) 32)
            (= (|POLY-ST->CNTM| st) 31)
            (= (|POLY-ST->CNTM| st) 30)
            (= (|POLY-ST->CNTM| st) 29)
            (= (|POLY-ST->CNTM| st) 28)
            (= (|POLY-ST->CNTM| st) 27)
            (= (|POLY-ST->CNTM| st) 26)
            (= (|POLY-ST->CNTM| st) 22)
            (= (|POLY-ST->CNTM| st) 21))
        0
      1))
   ((= (|POLY-ST->CNTP| st) 3)
    (if (= (|POLY-ST->CNTM| st) (+ 34 10 -18 -4))
        1
      0))
   ((= (|POLY-ST->CNTP| st) 4)
    (if (or (= (|POLY-ST->CNTM| st) (+ 34 10 -17 -4))
            (= (|POLY-ST->CNTM| st) (+ 34 10 -15 -4)))
        1
      0))
   ((= (|POLY-ST->CNTP| st) 5)
    (if (or (= (|POLY-ST->CNTM| st) (+ 34 10))
            (= (|POLY-ST->CNTM| st) (+ 33 10))
            (= (|POLY-ST->CNTM| st) (+ 32 10))
            (= (|POLY-ST->CNTM| st) (+ 31 10))
            (= (|POLY-ST->CNTM| st) (+ 34 10 -4))
            (= (|POLY-ST->CNTM| st) (+ 33 10 -4))
            (= (|POLY-ST->CNTM| st) (+ 32 10 -4))
            (= (|POLY-ST->CNTM| st) (+ 31 10 -4))
            (= (|POLY-ST->CNTM| st) (+ 30 10 -4))
            (= (|POLY-ST->CNTM| st) (+ 29 10 -4))
            (= (|POLY-ST->CNTM| st) (+ 28 10 -4))
            (= (|POLY-ST->CNTM| st) (+ 27 10 -4))
            (= (|POLY-ST->CNTM| st) (+ 26 10 -4))
            (= (|POLY-ST->CNTM| st) (+ 25 10 -4))
            (= (|POLY-ST->CNTM| st) (+ 24 10 -4))
            (= (|POLY-ST->CNTM| st) (+ 23 10 -4))
            (= (|POLY-ST->CNTM| st) (+ 21 10 -4))
            (= (|POLY-ST->CNTM| st) (+ 16 10 -4))
            (= (|POLY-ST->CNTM| st) (+ 15 10 -4)))
        0
      1))
   ((= (|POLY-ST->CNTP| st) 6)
    (if (or (= (|POLY-ST->CNTM| st) (+ 34 14 -20 -4))
            (= (|POLY-ST->CNTM| st) (+ 34 14 -13 -4)))
        1
      0))
   (t 0)))

(define |S2|
  ((st poly-st-p)
   (in poly-in-p))
  :returns (S2 unsigned-1-p)
  (b-xor (|S1| st in) (b-xor (|SL2| st) (|POLY-ST->P2| st))))

(define |P2-NEXT|
  ((st poly-st-p)
   (in poly-in-p)
   (fixbug booleanp))
  :returns (P2 unsigned-1-p)
  (if fixbug
      (cond ((= (|EN| st) 0)
             0)
            ((and (not (= (|EN| st) 0))
                  (= (|POLY-ST->DONE| st) 0)
                  (= (|POLY-ST->CNTS| st) 0)
                  (= (|POLY-ST->CNTM| st) 0))
             0)
            ((and (not (= (|EN| st) 0))
                  (= (|POLY-ST->DONE| st) 0)
                  (= (|POLY-ST->CNTS| st) 0))
             (b-ior
              (b-and (|S1| st in) (b-and (|SL2| st) (b-not (|POLY-ST->P2| st))))
              (b-ior
               (b-and (|S1| st in) (b-and (b-not (|SL2| st)) (|POLY-ST->P2| st)))
               (b-ior
                (b-and (b-not (|S1| st in)) (b-and (|SL2| st) (|POLY-ST->P2| st)))
                (b-and (|S1| st in) (b-and (|SL2| st) (|POLY-ST->P2| st)))))))
            (t (|POLY-ST->P2| st)))
    (cond ((= (|EN| st) 0)
           0)
          ((and (not (= (|EN| st) 0))
                (= (|POLY-ST->DONE| st) 0)
                (= (|POLY-ST->CNTS| st) 0))
           (b-ior
            (b-and (|S1| st in) (b-and (|SL2| st) (b-not (|POLY-ST->P2| st))))
            (b-ior
             (b-and (|S1| st in) (b-and (b-not (|SL2| st)) (|POLY-ST->P2| st)))
             (b-ior
              (b-and (b-not (|S1| st in)) (b-and (|SL2| st) (|POLY-ST->P2| st)))
              (b-and (|S1| st in) (b-and (|SL2| st) (|POLY-ST->P2| st)))))))
          ((and (not (= (|EN| st) 0))
                (= (|POLY-ST->DONE| st) 0)
                (= (|POLY-ST->CNTS| st) 0)
                (= (|POLY-ST->CNTM| st) 0))
           0)
          (t (|POLY-ST->P2| st)))))

(define |XS-NEXT|
  ((st poly-st-p)
   (in poly-in-p))
  :returns (XS unsigned-13-p)
  (loghead
   13
   (cond ((= (|EN| st) 0)
          (ash (|POLY-IN->INF| in) 3))
         ((and (not (= (|EN| st) 0))
               (= (|POLY-ST->DONE| st) 0)
               (= (|POLY-ST->CNTS| st) 0)
               (= (|POLY-ST->CNTM| st) 0)
               (= (|POLY-ST->CNTP| st) 0))
          (logior
           (ash (logbit 4 (|POLY-IN->SBIT| in)) 5)
           (ash (b-not (logbit 4 (|POLY-IN->SBIT| in))) 4)
           (logand (|POLY-IN->SBIT| in) #xF)))
         ((and (not (= (|EN| st) 0))
               (= (|POLY-ST->DONE| st) 0)
               (= (|POLY-ST->CNTS| st) 0)
               (= (|POLY-ST->CNTM| st) 0)
               (= (|POLY-ST->CNTP| st) 1))
          (logand (ash (|POLY-ST->RESULT| st) -5) #x1FFF))
         ((and (not (= (|EN| st) 0))
               (= (|POLY-ST->DONE| st) 0))
          (logior
           (ash (logbit 0 (|POLY-ST->XS| st)) 12)
           (ash (|POLY-ST->XS| st) -1)))
         (t (|POLY-ST->XS| st)))))

(define |RESULTn_1-NEXT|
  ((st poly-st-p))
  :returns (RESULTn_1 unsigned-1-p)
  (cond ((= (|EN| st) 0)
         0)
        ((and (not (= (|EN| st) 0))
              (= (|POLY-ST->DONE| st) 0))
         (cond ((and (= (|POLY-ST->CNTM| st) 0)
                     (= (|POLY-ST->CNTS| st) 0))
                0)
               ((and (not (= (|EN| st) 0))
                     (= (|POLY-ST->DONE| st) 0)
                     (>= (|POLY-ST->CNTM| st) (|POLY-ST->CNTD| st))
                     (= (|POLY-ST->CNTS| st) 0))
                (logbit 0 (|POLY-ST->RESULT| st)))
               ((and (not (= (|EN| st) 0))
                     (= (|POLY-ST->DONE| st) 0)
                     (= (|POLY-ST->CNTM| st) 0)
                     (= (|POLY-ST->CNTS| st) 0)
                     (= (|POLY-ST->CNTP| st) 6))
                0)
               (t (|POLY-ST->RESULTn_1| st))))
        ((and (not (= (|EN| st) 0))
              (not (= (|POLY-ST->DONE| st) 0))
              (not (= (|POLY-ST->ENshift_trig4| st) 0)))
         (logbit 0 (|POLY-ST->RESULT| st)))
        (t (|POLY-ST->RESULTn_1| st))))

(define |RESULTn_2-NEXT|
  ((st poly-st-p))
  :returns (RESULTn_2 unsigned-1-p)
  (cond ((= (|EN| st) 0)
         0)
        ((and (not (= (|EN| st) 0))
              (= (|POLY-ST->DONE| st) 0))
         (cond ((and (= (|POLY-ST->CNTM| st) 0)
                     (= (|POLY-ST->CNTS| st) 0))
                0)
               ((and (not (= (|EN| st) 0))
                     (= (|POLY-ST->DONE| st) 0)
                     (>= (|POLY-ST->CNTM| st) (|POLY-ST->CNTD| st))
                     (= (|POLY-ST->CNTS| st) 0))
                (|POLY-ST->RESULTn_1| st))
               ((and (not (= (|EN| st) 0))
                     (= (|POLY-ST->DONE| st) 0)
                     (= (|POLY-ST->CNTM| st) 0)
                     (= (|POLY-ST->CNTS| st) 0)
                     (= (|POLY-ST->CNTP| st) 6))
                0)
               (t (|POLY-ST->RESULTn_2| st))))
        ((and (not (= (|EN| st) 0))
              (not (= (|POLY-ST->DONE| st) 0))
              (not (= (|POLY-ST->ENshift_trig4| st) 0)))
         (|POLY-ST->RESULTn_1| st))
        (t (|POLY-ST->RESULTn_2| st))))

(define |RESULT-NEXT|
  ((st poly-st-p)
   (in poly-in-p))
  :returns (RESULT unsigned-35-p)
  (loghead
   35
   (cond ((= (|EN| st) 0)
          #ub11111_1111111111_1111111111_1111111111)
         ((and (not (= (|EN| st) 0))
               (= (|POLY-ST->DONE| st) 0))
          (cond ((and (= (|POLY-ST->CNTS| st) 0)
                      (not (and (= (|POLY-ST->CNTM| st) 0)
                                (= (|POLY-ST->CNTP| st) 1))))
                 (logior (ash (|S2| st in) 34)
                         (ash (|POLY-ST->RESULT| st) -1)))
                ((and (= (|POLY-ST->CNTS| st) 0)
                      (= (|POLY-ST->CNTM| st) 0)
                      (= (|POLY-ST->CNTP| st) 1))
                 (|POLY-IN->K5BIT| in))
                (t (|POLY-ST->RESULT| st))))
         ((and (not (= (|EN| st) 0))
               (not (= (|POLY-ST->DONE| st) 0))
               (not (= (|POLY-ST->ENshift_trig4| st) 0)))
          (logior (logand (|POLY-ST->RESULT| st) (lognot #xFFF))
                  #x800
                  (logand (ash (|POLY-ST->RESULT| st) -1) #x7FF)))
         (t (|POLY-ST->RESULT| st)))))

(define |WORK-NEXT|
  ((st poly-st-p)
   (in poly-in-p))
  :returns (RESULT unsigned-13-p)
  (loghead
   13
   (cond ((= (|EN| st) 0)
          (|POLY-IN->TT| in))
         ((and (not (= (|EN| st) 0))
               (= (|POLY-ST->DONE| st) 0))
          (cond ((not (= (|POLY-ST->CNTS| st) 0))
                 (logior (ash (|S_| st) 12)
                         (ash (|POLY-ST->WORK| st) -1)))
                ((and (= (|POLY-ST->CNTS| st) 0)
                      (not (= (|POLY-ST->CNTM| st) 0)))
                 (logior (ash (|S_| st) 12)
                         (ash (|S_| st) 11)
                         (ash (|POLY-ST->WORK| st) -2)))
                ((and (= (|POLY-ST->CNTS| st) 0)
                      (= (|POLY-ST->CNTM| st) 0))
                 0)))
         (t (|POLY-ST->WORK| st)))))

(define |RESULTout-NEXT|
  ((st poly-st-p))
  :returns (RESULT unsigned-12-p)
  (loghead
   12
   (if (not (= (|POLY-ST->DONE| st) 0))
       (cond ((logbitp 34 (|POLY-ST->RESULT| st))
              #x000)
             ((not (= (logand (|POLY-ST->RESULT| st) #x3FFFFF000) 0))
              #xFFF)
             (t (logand (|POLY-ST->RESULT| st) #xFFF)))
     (|POLY-ST->RESULTout| st))))

(define posedge
  ((st poly-st-p)
   (in poly-in-p)
   (fixbug booleanp))
  :returns (st poly-st-p)
  (make-poly-st
   :|RESULTout|      (|RESULTout-NEXT| st)
   :|XS|             (|XS-NEXT| st in)
   :|RESULT|         (|RESULT-NEXT| st in)
   :|WORK|           (|WORK-NEXT| st in)
   :|P|              (|P-NEXT| st)
   :|P1|             (|P1-NEXT| st in fixbug)
   :|P2|             (|P2-NEXT| st in fixbug)
   :|RESULTn_1|      (|RESULTn_1-NEXT| st)
   :|RESULTn_2|      (|RESULTn_2-NEXT| st)
   :|CNTP|           (|POLY-ST->CNTP| st)
   :|CNTS|           (|POLY-ST->CNTS| st)
   :|CNTM|           (|POLY-ST->CNTM| st)
   :|CNTD|           (|POLY-ST->CNTD| st)
   :|DONE|           (|POLY-ST->DONE| st)
   :|ENwork_trig0|   (|POLY-IN->ENwork| in)
   :|ENwork_trig1|   (|POLY-ST->ENwork_trig0| st)
   :|ENshift_trig0|  (|POLY-IN->ENshift| in)
   :|ENshift_trig1|  (|POLY-ST->ENshift_trig0| st)
   :|ENshift_trig2|  (|POLY-ST->ENshift_trig1| st)
   :|ENshift_trig3|  (|POLY-ST->ENshift_trig2| st)
   :|ENshift_trig4|  (|POLY-ST->ENshift_trig3| st)
   :|EN_trig|        (|EN| st)))

(define clk
  ((st poly-st-p)
   (in poly-in-p)
   (fixbug booleanp))
  :returns (st poly-st-p)
  (negedge (posedge st in fixbug)))

