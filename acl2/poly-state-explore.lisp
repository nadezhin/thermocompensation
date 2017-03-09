(in-package "ACL2")
(include-book "poly-state")

(local (include-book "centaur/bitops/ihsext-basics" :dir :system))

(rule
 (let ((nst (clk st in fixbug)))
   (and
    (equal (|POLY-ST->ENwork_trig0|  nst) (|POLY-IN->ENwork|        in))
    (equal (|POLY-ST->ENwork_trig1|  nst) (|POLY-ST->ENwork_trig0|  st))
    (equal (|POLY-ST->ENshift_trig0| nst) (|POLY-IN->ENshift|       in))
    (equal (|POLY-ST->ENshift_trig1| nst) (|POLY-ST->ENshift_trig0| st))
    (equal (|POLY-ST->ENshift_trig2| nst) (|POLY-ST->ENshift_trig1| st))
    (equal (|POLY-ST->ENshift_trig3| nst) (|POLY-ST->ENshift_trig2| st))
    (equal (|POLY-ST->ENshift_trig4| nst) (|POLY-ST->ENshift_trig3| st))
    (equal (|POLY-ST->EN_trig|       nst) (|EN|                     st))))
 :enable (clk posedge negedge))

(define clip
  ((RESULT unsigned-35-p))
  :returns (RESULTout unsigned-12-p
                      :hints (("goal" :in-theory (disable unsigned-byte-p))))
  (cond ((logbitp 34 RESULT) #x000)
        ((not (= (logand RESULT #x3FFFFF000) 0)) #xFFF)
        (t (loghead 12 (logand RESULT #xFFF))))
  ///
  (defruled |RESULTout-NEXT as clip|
    (equal (|RESULTout-NEXT| st)
           (if (= (|POLY-ST->DONE| st) 0)
               (|POLY-ST->RESULTout| st)
             (clip (|POLY-ST->RESULT| st))))
    :enable |RESULTout-NEXT|))

(define shiftout
  ((RESULT unsigned-35-p))
  :returns (shifted unsigned-35-p)
  (loghead
   35
   (logior (logand RESULT (lognot #xFFF))
           #x800
           (logand (ash RESULT -1) #x7FF)))
  ///
  (defruled |RESULT-NEXT when DONE|
    (implies (and (= (|EN| st) 1)
                  (= (|POLY-ST->DONE| st) 1))
             (equal (|RESULT-NEXT| st in)
                    (if (= (|POLY-ST->ENshift_trig4| st) 1)
                        (shiftout (|POLY-ST->RESULT| st))
                      (|POLY-ST->RESULT| st))))
    :enable |RESULT-NEXT|))

(rule
 (let ((nst (clk st in fixbug)))
   (implies
    (equal (|EN| st) 0)
    (and
     (equal (|POLY-ST->RESULTout| nst) (if (= (|POLY-ST->DONE| st) 0)
                                           (|POLY-ST->RESULTout| st)
                                         (clip (|POLY-ST->RESULT| st))))
     (equal (|POLY-ST->XS|        nst) (loghead 13 (ash (|POLY-IN->INF| in) 3)))
     (equal (|POLY-ST->RESULT|    nst) #ub11111_1111111111_1111111111_1111111111)
     (equal (|POLY-ST->WORK|      nst) (loghead 13 (|POLY-IN->TT| in)))
     (equal (|POLY-ST->P|         nst) 0)
     (equal (|POLY-ST->P1|        nst) 0)
     (equal (|POLY-ST->P2|        nst) 0)
     (equal (|POLY-ST->RESULTn_1| nst) 0)
     (equal (|POLY-ST->RESULTn_2| nst) 0)
     (equal (|POLY-ST->CNTP|      nst) 0)
     (equal (|POLY-ST->CNTS|      nst) 12)
     (equal (|POLY-ST->CNTM|      nst) 34)
     (equal (|POLY-ST->CNTD|      nst) 0)
     (equal (|POLY-ST->DONE|      nst) 0)
     (equal (|POLY-ST->EN_trig|   nst) 0))))
 :enable (clk posedge negedge |RESULTout-NEXT as clip|)
 :prep-lemmas
 ((defrule XS-lemma
    (implies (equal (|EN| st) 0)
             (equal (|XS-NEXT| st in)
                    (loghead 13 (ash (|POLY-IN->INF| in) 3))))
    :enable |XS-NEXT|)
  (defrule RESULT-lemma
    (implies (equal (|EN| st) 0)
             (equal (|RESULT-NEXT| st in)
                    #ub11111_1111111111_1111111111_1111111111))
    :enable |RESULT-NEXT|)
  (defrule WORK-lemma
    (implies (equal (|EN| st) 0)
             (equal (|WORK-NEXT| st in)
                    (loghead 13  (|POLY-IN->TT| in))))
    :enable |WORK-NEXT|)
  (defrule P-lemma
    (implies (equal (|EN| st) 0)
             (equal (|P-NEXT| st) 0))
    :enable |P-NEXT|)
  (defrule P1-lemma
    (implies (equal (|EN| st) 0)
             (equal (|P1-NEXT| st in fuxbug) 0))
    :enable |P1-NEXT|)
  (defrule P2-lemma
    (implies (equal (|EN| st) 0)
             (equal (|P2-NEXT| st in fixbug) 0))
    :enable |P2-NEXT|)
  (defrule RESULTn_1-lemma
    (implies (equal (|EN| st) 0)
             (equal (|RESULTn_1-NEXT| st) 0))
    :enable |RESULTn_1-NEXT|)
  (defrule RESULTn_2-lemma
    (implies (equal (|EN| st) 0)
             (equal (|RESULTn_2-NEXT| st) 0))
    :enable |RESULTn_2-NEXT|)
  (defrule CNTP-lemma
    (implies (equal (|POLY-ST->EN_trig| st) 0)
             (equal (|NEXT-CNTP| st) 0))
    :enable |NEXT-CNTP|)
  (defrule CNTS-lemma
    (implies (equal (|POLY-ST->EN_trig| st) 0)
             (equal (|NEXT-CNTS| st) 12))
    :enable |NEXT-CNTS|)
  (defrule CNTM-lemma
    (implies (equal (|POLY-ST->EN_trig| st) 0)
             (equal (|NEXT-CNTM| st) 34))
    :enable |NEXT-CNTM|)
  (defrule CNTD-lemma
    (implies (equal (|POLY-ST->EN_trig| st) 0)
             (equal (|NEXT-CNTD| st) 0))
    :enable |NEXT-CNTD|)
  (defrule DONE-lemma
    (implies (equal (|POLY-ST->EN_trig| st) 0)
             (equal (|NEXT-DONE| st) 0))
    :enable |NEXT-DONE|)))


(rule
 (let ((nst (clk st in fixbug)))
   (implies
    (and (equal (|EN| st) 1)
         (equal (|POLY-ST->DONE| st) 1))
    (and
     (equal (|POLY-ST->RESULTout| nst) (clip (|POLY-ST->RESULT| st)))
     (equal (|POLY-ST->XS|        nst) (|POLY-ST->XS| st))
     (equal (|POLY-ST->RESULT|    nst) (if (= (|POLY-ST->ENshift_trig4| st) 1)
                                           (shiftout (|POLY-ST->RESULT| st))
                                         (|POLY-ST->RESULT| st)))
     (equal (|POLY-ST->WORK|      nst) (|POLY-ST->WORK| st))
     (equal (|POLY-ST->P|         nst) (|POLY-ST->P| st))
     (equal (|POLY-ST->P1|        nst) (|POLY-ST->P1| st))
     (equal (|POLY-ST->P2|        nst) (|POLY-ST->P2| st))
     (equal (|POLY-ST->RESULTn_1| nst) (|POLY-ST->RESULTn_1| st))
     (equal (|POLY-ST->RESULTn_2| nst) (|POLY-ST->RESULTn_2| st))
     (equal (|POLY-ST->CNTP|      nst) (if (and (= (|POLY-ST->CNTS| st) 0)
                                                (= (|POLY-ST->CNTM| st) 0))
                                           (loghead 3 (1+ (|POLY-ST->CNTP| st)))
                                         (|POLY-ST->CNTP| st)))
     (equal (|POLY-ST->CNTS|      nst) (|POLY-ST->CNTS| st))
     (equal (|POLY-ST->CNTM|      nst) (|POLY-ST->CNTM| st))
     (equal (|POLY-ST->CNTD|      nst) (|POLY-ST->CNTD| st))
     (equal (|POLY-ST->DONE|      nst) (if (and (= (|POLY-ST->CNTS| st) 0)
                                                (= (|POLY-ST->CNTM| st) 0))
                                           (bool->bit (= (|POLY-ST->CNTP| st) 6))
                                         (|POLY-ST->DONE| st)))
     (equal (|POLY-ST->EN_trig|   nst) 1))))
 :enable (clk posedge negedge
              |RESULTout-NEXT as clip|
              |RESULT-NEXT when DONE|)
 :prep-lemmas
 ((defrule XS-lemma
    (implies (and (equal (|EN| st) 1) (equal (|POLY-ST->DONE| st) 1))
             (equal (|XS-NEXT| st in) (|POLY-ST->XS| st)))
    :enable |XS-NEXT|)
  (defrule WORK-lemma
    (implies (and (equal (|EN| st) 1) (equal (|POLY-ST->DONE| st) 1))
             (equal (|WORK-NEXT| st in) (|POLY-ST->WORK| st)))
    :enable |WORK-NEXT|)
  (defrule P-lemma
    (implies (and (equal (|EN| st) 1) (equal (|POLY-ST->DONE| st) 1))
             (equal (|P-NEXT| st) (|POLY-ST->P| st)))
    :enable |P-NEXT|)
  (defrule P1-lemma
    (implies (and (equal (|EN| st) 1) (equal (|POLY-ST->DONE| st) 1))
             (equal (|P1-NEXT| st in fixbug) (|POLY-ST->P1| st)))
    :enable |P1-NEXT|)
  (defrule P2-lemma
    (implies (and (equal (|EN| st) 1) (equal (|POLY-ST->DONE| st) 1))
             (equal (|P2-NEXT| st in fixbug) (|POLY-ST->P2| st)))
    :enable |P2-NEXT|)
  (defrule RESULTn_1-lemma
    (implies (and (equal (|EN| st) 1) (equal (|POLY-ST->DONE| st) 1))
             (equal (|RESULTn_1-NEXT| st) (|POLY-ST->RESULTn_1| st)))
    :enable |RESULTn_1-NEXT|)
  (defrule RESULTn_2-lemma
    (implies (and (equal (|EN| st) 1) (equal (|POLY-ST->DONE| st) 1))
             (equal (|RESULTn_2-NEXT| st) (|POLY-ST->RESULTn_2| st)))
    :enable |RESULTn_2-NEXT|)
  (defrule CNTP-lemma
    (implies (and (equal (|POLY-ST->EN_trig| st) 1) (equal (|POLY-ST->DONE| st) 1))
             (equal (|NEXT-CNTP| st)
                    (if (and (= (|POLY-ST->CNTS| st) 0)
                             (= (|POLY-ST->CNTM| st) 0))
                        (loghead 3 (1+ (|POLY-ST->CNTP| st)))
                      (|POLY-ST->CNTP| st))))
    :enable |NEXT-CNTP|)
  (defrule CNTS-lemma
    (implies (and (equal (|POLY-ST->EN_trig| st) 1) (equal (|POLY-ST->DONE| st) 1))
             (equal (|NEXT-CNTS| st) (|POLY-ST->CNTS| st)))
    :enable |NEXT-CNTS|)
  (defrule CNTM-lemma
    (implies (and (equal (|POLY-ST->EN_trig| st) 1) (equal (|POLY-ST->DONE| st) 1))
             (equal (|NEXT-CNTM| st) (|POLY-ST->CNTM| st)))
    :enable |NEXT-CNTM|)
  (defrule CNTD-lemma
    (implies (and (equal (|POLY-ST->EN_trig| st) 1) (equal (|POLY-ST->DONE| st) 1))
             (equal (|NEXT-CNTD| st) (|POLY-ST->CNTD| st)))
    :enable |NEXT-CNTD|)
  (defrule DONE-lemma
    (implies (and (equal (|POLY-ST->EN_trig| st) 1) (equal (|POLY-ST->DONE| st) 1))
             (equal (|NEXT-DONE| st)
                    (if (and (= (|POLY-ST->CNTS| st) 0)
                             (= (|POLY-ST->CNTM| st) 0))
                        (bool->bit (= (|POLY-ST->CNTP| st) 6))
                      (|POLY-ST->DONE| st))))
    :enable |NEXT-DONE|)))


(rule
 (let ((nst (clk st in fixbug)))
   (implies
    (and (equal (|EN| st) 1)
         (equal (|POLY-ST->DONE| st) 0)
         (not (equal (|POLY-ST->CNTS| st) 0)))
    (and
     (equal (|POLY-ST->RESULTout| nst) (|POLY-ST->RESULTout| st))
     (equal (|POLY-ST->XS|        nst) (loghead
                                        13
                                        (logior
                                         (ash (logbit 0 (|POLY-ST->XS| st)) 12)
                                         (ash (|POLY-ST->XS| st) -1))))
     (equal (|POLY-ST->RESULT|    nst) (|POLY-ST->RESULT| st))
     (equal (|POLY-ST->WORK|      nst) (loghead
                                        13
                                        (logior (ash (|S| st) 12)
                                                (ash (|POLY-ST->WORK| st) -1))))
     (equal (|POLY-ST->P|         nst) (b-ior
                                        (b-and (|S1in| st) (b-and (|S2in| st) (b-not (|Pin| st))))
                                        (b-ior
                                         (b-and (|S1in| st) (b-and (b-not (|S2in| st)) (|Pin| st)))
                                         (b-ior
                                          (b-and (b-not (|S1in| st)) (b-and (|S2in| st) (|Pin| st)))
                                          (b-and (|S1in| st) (b-and (|S2in| st) (|Pin| st)))))))
     (equal (|POLY-ST->P1|        nst) (|POLY-ST->P1| st))
     (equal (|POLY-ST->P2|        nst) (|POLY-ST->P2| st))
     (equal (|POLY-ST->RESULTn_1| nst) (|POLY-ST->RESULTn_1| st))
     (equal (|POLY-ST->RESULTn_2| nst) (|POLY-ST->RESULTn_2| st))
     (equal (|POLY-ST->CNTP|      nst) (|POLY-ST->CNTP| st))
     (equal (|POLY-ST->CNTS|      nst) (1- (|POLY-ST->CNTS| st)))
     (equal (|POLY-ST->CNTM|      nst) (|POLY-ST->CNTM| st))
     (equal (|POLY-ST->CNTD|      nst) (|POLY-ST->CNTD| st))
     (equal (|POLY-ST->DONE|      nst) 0)
     (equal (|POLY-ST->EN_trig|   nst) 1))))
 :enable (clk posedge negedge)
 :prep-lemmas
 ((defrule RESULTout-lemma
    (implies (and (= (|EN| st) 1) (= (|POLY-ST->DONE| st) 0))
             (equal (|RESULTout-NEXT| st) (|POLY-ST->RESULTout| st)))
    :enable |RESULTout-NEXT|)
  (defrule XS-lemma
    (implies (and (= (|EN| st) 1) (= (|POLY-ST->DONE| st) 0) (not (= (|POLY-ST->CNTS| st) 0)))
             (equal (|XS-NEXT| st in)
                    (loghead
                     13
                     (logior
                      (ash (logbit 0 (|POLY-ST->XS| st)) 12)
                      (ash (|POLY-ST->XS| st) -1)))))
    :enable |XS-NEXT|)
  (defrule RESULT-lemma
    (implies (and (= (|EN| st) 1) (= (|POLY-ST->DONE| st) 0) (not (= (|POLY-ST->CNTS| st) 0)))
             (equal (|RESULT-NEXT| st in) (|POLY-ST->RESULT| st)))
    :enable |RESULT-NEXT|)
  (defrule WORK-lemma
    (implies (and (= (|EN| st) 1) (= (|POLY-ST->DONE| st) 0) (not (= (|POLY-ST->CNTS| st) 0)))
             (equal (|WORK-NEXT| st in)
                    (loghead
                     13
                     (logior (ash (|S| st) 12)
                             (ash (|POLY-ST->WORK| st) -1)))))
    :enable |WORK-NEXT|)
  (defrule P-lemma
    (implies (and (= (|EN| st) 1) (= (|POLY-ST->DONE| st) 0) (not (= (|POLY-ST->CNTS| st) 0)))
             (equal (|P-NEXT| st)
                    (b-ior
                     (b-and (|S1in| st) (b-and (|S2in| st) (b-not (|Pin| st))))
                     (b-ior
                      (b-and (|S1in| st) (b-and (b-not (|S2in| st)) (|Pin| st)))
                      (b-ior
                       (b-and (b-not (|S1in| st)) (b-and (|S2in| st) (|Pin| st)))
                       (b-and (|S1in| st) (b-and (|S2in| st) (|Pin| st))))))))
    :enable |P-NEXT|)
  (defrule P1-lemma
    (implies (and (= (|EN| st) 1) (= (|POLY-ST->DONE| st) 0) (not (= (|POLY-ST->CNTS| st) 0)))
             (equal (|P1-NEXT| st in fixbug) (|POLY-ST->P1| st)))
    :enable |P1-NEXT|)
  (defrule P2-lemma
    (implies (and (= (|EN| st) 1) (= (|POLY-ST->DONE| st) 0) (not (= (|POLY-ST->CNTS| st) 0)))
             (equal (|P2-NEXT| st in fixbug) (|POLY-ST->P2| st)))
    :enable |P2-NEXT|)
  (defrule RESULTn_1-lemma
    (implies (and (= (|EN| st) 1) (= (|POLY-ST->DONE| st) 0) (not (= (|POLY-ST->CNTS| st) 0)))
             (equal (|RESULTn_1-NEXT| st) (|POLY-ST->RESULTn_1| st)))
    :enable |RESULTn_1-NEXT|)
  (defrule RESULTn_2-lemma
    (implies (and (= (|EN| st) 1) (= (|POLY-ST->DONE| st) 0) (not (= (|POLY-ST->CNTS| st) 0)))
             (equal (|RESULTn_2-NEXT| st) (|POLY-ST->RESULTn_2| st)))
    :enable |RESULTn_2-NEXT|)
  (defrule CNTP-lemma
    (implies (and (= (|POLY-ST->EN_trig| st) 1) (= (|POLY-ST->DONE| st) 0) (not (= (|POLY-ST->CNTS| st) 0)))
             (equal (|NEXT-CNTP| st) (|POLY-ST->CNTP| st)))
    :enable |NEXT-CNTP|)
  (defrule CNTS-lemma
    (implies (and (= (|POLY-ST->EN_trig| st) 1) (= (|POLY-ST->DONE| st) 0) (not (= (|POLY-ST->CNTS| st) 0)))
             (equal (|NEXT-CNTS| st) (1- (|POLY-ST->CNTS| st))))
    :enable |NEXT-CNTS|)
  (defrule CNTM-lemma
    (implies (and (= (|POLY-ST->EN_trig| st) 1) (= (|POLY-ST->DONE| st) 0) (not (= (|POLY-ST->CNTS| st) 0)))
             (equal (|NEXT-CNTM| st) (|POLY-ST->CNTM| st)))
    :enable |NEXT-CNTM|)
  (defrule CNTD-lemma
    (implies (and (= (|POLY-ST->EN_trig| st) 1) (= (|POLY-ST->DONE| st) 0) (not (= (|POLY-ST->CNTS| st) 0)))
             (equal (|NEXT-CNTD| st) (|POLY-ST->CNTD| st)))
    :enable |NEXT-CNTD|)
  (defrule DONE-lemma
    (implies (and (= (|POLY-ST->EN_trig| st) 1) (= (|POLY-ST->DONE| st) 0) (not (= (|POLY-ST->CNTS| st) 0)))
             (equal (|NEXT-DONE| st) 0))
    :enable |NEXT-DONE|)
  )
 )



(rule
 (and
  (equal (|POLY-ST->RESULTout| (clk (reset) in fixbug)) 0)
  (equal (|POLY-ST->XS| (clk (reset) in fixbug)) (LOGHEAD 13 (ASH (POLY-IN->INF IN) 3)))
  (equal (|POLY-ST->RESULT| (clk (reset) in fixbug)) #ub11111_1111111111_1111111111_1111111111)
  (equal (|POLY-ST->WORK| (clk (reset) in fixbug)) (LOGHEAD 13 (POLY-IN->TT IN)))
  (equal (|POLY-ST->P| (clk (reset) in fixbug)) 0)
  (equal (|POLY-ST->P1| (clk (reset) in fixbug)) 0)
  (equal (|POLY-ST->P2| (clk (reset) in fixbug)) 0)
  (equal (|POLY-ST->RESULTn_1| (clk (reset) in fixbug)) 0)
  (equal (|POLY-ST->RESULTn_2| (clk (reset) in fixbug)) 0)
  (equal (|POLY-ST->CNTP| (clk (reset) in fixbug)) 0)
  (equal (|POLY-ST->CNTS| (clk (reset) in fixbug)) 12)
  (equal (|POLY-ST->CNTM| (clk (reset) in fixbug)) 34)
  (equal (|POLY-ST->CNTD| (clk (reset) in fixbug)) 0)
  (equal (|POLY-ST->DONE| (clk (reset) in fixbug)) 0)
  (equal (|POLY-ST->ENwork_trig0| (clk (reset) in fixbug)) (|POLY-IN->ENwork| in))
  (equal (|POLY-ST->ENwork_trig1| (clk (reset) in fixbug)) 0)
  (equal (|POLY-ST->ENshift_trig0| (clk (reset) in fixbug)) (|POLY-IN->ENshift| in))
  (equal (|POLY-ST->ENshift_trig1| (clk (reset) in fixbug)) 0)
  (equal (|POLY-ST->ENshift_trig2| (clk (reset) in fixbug)) 0)
  (equal (|POLY-ST->ENshift_trig3| (clk (reset) in fixbug)) 0)
  (equal (|POLY-ST->ENshift_trig4| (clk (reset) in fixbug)) 0)
  (equal (|POLY-ST->EN_trig| (clk (reset) in fixbug)) 0))
 :enable (clk posedge negedge
              |XS-NEXT|
              |RESULT-NEXT|
              |WORK-NEXT|
              |P1-NEXT|
              |P2-NEXT|
              |NEXT-CNTP|
              |NEXT-CNTS|
              |NEXT-CNTM|
              |NEXT-CNTD|
              |NEXT-DONE|))

(rule
 (let ((nst (clk st in fixbug)))
   (implies (and (= (|POLY-ST->DONE| st) 0)
                 (= (|POLY-ST->DONE| nst) 1))
            (and
             (equal (|POLY-ST->CNTP| nst) 7)
             (equal (|POLY-ST->CNTS| nst) 12)
             (equal (|POLY-ST->CNTM| nst) 0)
             (equal (|POLY-ST->CNTD| nst) 0)
             )))
 :enable (clk posedge negedge
              |NEXT-CNTP|
              |NEXT-CNTS|
              |NEXT-CNTM|
              |NEXT-CNTD|
              |NEXT-DONE|))

(rule
 (let ((nst (clk st in fixbug)))
   (implies (and (= (|EN| st) 1)
                 (= (|POLY-ST->DONE| st) 1)
                 (= (|POLY-ST->CNTP| st) 7)
                 (= (|POLY-ST->CNTS| st) 12)
                 (= (|POLY-ST->CNTM| st) 0)
                 (= (|POLY-ST->CNTD| st) 0))
            (and (equal (|POLY-ST->DONE| nst) 1)
                 (equal (|POLY-ST->CNTP| nst) 7)
                 (equal (|POLY-ST->CNTS| nst) 12)
                 (equal (|POLY-ST->CNTM| nst) 0)
                 (equal (|POLY-ST->CNTD| nst) 0))))
 :enable (clk posedge negedge
              |NEXT-CNTP|
              |NEXT-CNTS|
              |NEXT-CNTM|
              |NEXT-CNTD|
              |NEXT-DONE|))

(rule
 (let ((nst (clk st in fixbug)))
   (implies (and (= (|EN| st) 0)
                 (= (|POLY-ST->DONE| st) 1)
                 (= (|POLY-ST->CNTP| st) 7)
                 (= (|POLY-ST->CNTS| st) 12)
                 (= (|POLY-ST->CNTM| st) 0)
                 (= (|POLY-ST->CNTD| st) 0))
            (and (equal (|POLY-ST->DONE| nst) 0)
                 (equal (|POLY-ST->CNTP| nst) 0)
                 (equal (|POLY-ST->CNTS| nst) 12)
                 (equal (|POLY-ST->CNTM| nst) 34)
                 (equal (|POLY-ST->CNTD| nst) 0))))
 :enable (clk posedge negedge
              |NEXT-CNTP|
              |NEXT-CNTS|
              |NEXT-CNTM|
              |NEXT-CNTD|
              |NEXT-DONE|))

(define initial-p
  ((st poly-st-p))
  :returns (ok booleanp)
  (and (= (|POLY-ST->RESULT| st) #x7FFFFFFFF)
       (= (|POLY-ST->P| st) 0)
       (= (|POLY-ST->P1| st) 0)
       (= (|POLY-ST->P2| st) 0)
       (= (|POLY-ST->RESULTn_1| st) 0)
       (= (|POLY-ST->RESULTn_2| st) 0)
       (= (|POLY-ST->CNTP| st) 0)
       (= (|POLY-ST->CNTS| st) 12)
       (= (|POLY-ST->CNTM| st) 34)
       (= (|POLY-ST->CNTD| st) 0)
       (= (|POLY-ST->DONE| st) 0)
       (= (|POLY-ST->EN_trig| st) 0)))

(define work-p
  ((st poly-st-p))
  :returns (ok booleanp)
  (let ((cntm (|POLY-ST->CNTM| st))
        (cntd (|POLY-ST->CNTD| st)))
    (and (<= (|POLY-ST->CNTS| st) 12)
         (case (|POLY-ST->CNTP| st)
           (0 (and (<=  cntm 34)  (= cntd 0)))
           (1 (and (<= cntm 35) (= cntd 0)))
           (2 (and (<= cntm 34) (= cntd 0)))
           (3 (and (<= cntm 44) (= cntd 10)))
           (4 (and (<= cntm 44) (= cntd 10)))
           (5 (and (<= cntm 44) (= cntd 10)))
           (6 (and (<= cntm 48) (= cntd 14))))
         (= (|POLY-ST->DONE| st) 0)
         (= (|POLY-ST->EN_trig| st) 1))))

(define done-p
  ((st poly-st-p))
  :returns (ok booleanp)
  (and (= (|POLY-ST->CNTP| st) 7)
       (= (|POLY-ST->CNTS| st) 12)
       (= (|POLY-ST->CNTM| st) 0)
       (= (|POLY-ST->CNTD| st) 0)
       (= (|POLY-ST->DONE| st) 1)
       (= (|POLY-ST->EN_trig| st) 1)))

(rule
 (initial-p (reset)))

(rule
 (implies (= (|EN| st) 0)
          (initial-p (clk st in fixbug)))
 :enable (clk posedge negedge
              initial-p
              |RESULT-NEXT|
              |P-NEXT|
              |P1-NEXT|
              |P2-NEXT|
              |RESULTn_1-NEXT|
              |RESULTn_2-NEXT|
              |NEXT-CNTP|
              |NEXT-CNTS|
              |NEXT-CNTM|
              |NEXT-CNTD|
              |NEXT-DONE|))

(rule
 (implies (and (initial-p st)
               (= (|EN| st) 1))
          (work-p (clk st in fixbug)))
 :enable (clk posedge negedge
              initial-p
              work-p
              |NEXT-CNTP|
              |NEXT-CNTS|
              |NEXT-CNTM|
              |NEXT-CNTD|
              |NEXT-DONE|))

(rule
 (implies (and (work-p st)
               (= (|EN| st) 1))
          (or (work-p (clk st in fixbug))
              (done-p (clk st in fixbug))))
 :enable (clk posedge negedge
              work-p
              done-p
              |NEXT-CNTP|
              |NEXT-CNTS|
              |NEXT-CNTM|
              |NEXT-CNTD|
              |NEXT-DONE|))

(rule
 (implies (and (done-p st)
               (= (|EN| st) 1))
          (done-p (clk st in fixbug)))
 :enable (clk posedge negedge
              done-p
              |NEXT-CNTP|
              |NEXT-CNTS|
              |NEXT-CNTM|
              |NEXT-CNTD|
              |NEXT-DONE|))

(rule
 (let ((nst (clk st in fixbug)))
   (implies (and (work-p st)
                 (done-p nst))
            (and (equal (|POLY-ST->CNTP| st) 6)
                 (equal (|POLY-ST->CNTS| st) 0)
                 (equal (|POLY-ST->CNTM| st) 0)
                 (equal (|POLY-ST->CNTD| st) 14))))
 :enable (clk posedge negedge
              work-p done-p
              |NEXT-CNTP|))

(rule
 (let ((nst (clk st in fixbug)))
   (implies (and (work-p st)
                 (done-p nst))
            (and (equal (|POLY-ST->RESULTout| nst) (|POLY-ST->RESULTout| st))
                 (equal (|POLY-ST->RESULT| nst) (loghead
                                                 35
                                                 (logior (ash (|S2| st in) 34)
                                                         (ash (|POLY-ST->RESULT| st) -1)))))))
 :enable (clk posedge negedge
              work-p done-p
              |NEXT-DONE|
              |RESULTout-NEXT|
              |RESULT-NEXT|))

(rule
 (let ((nst (clk st in fixbug)))
   (implies (and (done-p st)
                 (= (|EN| st) 1))
            (and (equal (|POLY-ST->RESULTout| nst) (clip (|POLY-ST->RESULT| st)))
                 (equal (|POLY-ST->RESULT| nst)
                        (if (= (|POLY-ST->ENshift_trig4| st) 0)
                            (|POLY-ST->RESULT| st)
                          (logior (logand (|POLY-ST->RESULT| st) (lognot #xFFF))
                                  #x800
                                  (logand (ash (|POLY-ST->RESULT| st) -1) #x7FF)))))))
 :enable (clk posedge negedge
              done-p
              |RESULTout-NEXT as clip|
              |RESULT-NEXT|))

(define invariant
  ((st poly-st-p))
  :returns (ok booleanp)
  (or (initial-p st)
      (work-p st)
      (done-p st)))

(rule
 (invariant (reset)))

(rule
 (implies (invariant st)
          (invariant (clk st in fixbug)))
 :enable (clk posedge negedge
              invariant
              initial-p
              work-p
              done-p
              |RESULT-NEXT|
              |P-NEXT|
              |P1-NEXT|
              |P2-NEXT|
              |RESULTn_1-NEXT|
              |RESULTn_2-NEXT|
              |NEXT-CNTP|
              |NEXT-CNTS|
              |NEXT-CNTM|
              |NEXT-CNTD|
              |NEXT-DONE|))
