(in-package "ACL2")
(include-book "centaur/sv/svtv/process" :dir :system)
(include-book "polinom_newK_final_out")
(include-book "../poly-state")

(local (include-book "centaur/gl/gl" :dir :system))
(local (include-book "centaur/gl/bfr-satlink" :dir :system))

(local (progn (defun my-satlink-config
                     nil (declare (xargs :guard t))
                     (satlink::make-config :cmdline "glucose-cert"
                                           :verbose t
                                           :mintime 1))
              (defattach gl::gl-satlink-config
                         my-satlink-config)))

(local (gl::gl-satlink-mode))

(value-triple (acl2::tshell-start))

(defsvtv polinom-posedge
  :mod *polinom_out*
  :inputs
  '(("CLK"     0 1)
    ("RST"     0 0)
    ("K1BIT"   |K1BIT| _)
    ("K2BIT"   |K2BIT| _)
    ("K3BIT"   |K3BIT| _)
    ("K4BIT"   |K4BIT| _)
    ("K5BIT"   |K5BIT| _)
    ("SBIT"    |SBIT| _)
    ("INF"     |INF| _)
    ("T"       |TT| _)
    ("ENwork"  |ENwork| _)
    ("ENshift" |ENshift| _)
    )
  :overrides
  '(("RESULTout"                |RESULTout-s|     _) ; 12
    ("XS"                       |XS-s|            _) ; 13
    ("RESULT"                   |RESULT-s|        _) ; 35
    ("WORK"                     |WORK-s|          _) ; 13
    ("P"                        |P-s|             _) ;  1
    ("P1"                       |P1-s|            _) ;  1
    ("P2"                       |P2-s|            _) ;  1
    ("RESULTn_1"                |RESULTn_1-s|     _) ;  1
    ("RESULTn_2"                |RESULTn_2-s|     _) ;  1
    ("CNTP"                     |CNTP-s|          _) ;  3
    ("CNTS"                     |CNTS-s|          _) ;  4
    ("CNTM"                     |CNTM-s|          _) ;  6
    ("CNTD"                     |CNTD-s|          _) ;  6
    ("n_1499"                   |DONE-s|          _) ;  1
    ("ENwork_trig0"             |ENwork_trig0-s|  _) ;  1
    ("ENwork_trig1"             |ENwork_trig1-s|  _) ;  1
    ("ENshift_trig0"            |ENshift_trig0-s| _) ;  1
    ("ENshift_trig1"            |ENshift_trig1-s| _) ;  1
    ("ENshift_trig2"            |ENshift_trig2-s| _) ;  1
    ("ENshift_trig3"            |ENshift_trig3-s| _) ;  1
    ("ENshift_trig4"            |ENshift_trig4-s| _) ;  1
    ("EN_trig"                  |EN_trig-s|       _)) ;  1
  :internals
  '(("RESULTout"                _ |RESULTout-d|    ) ; 12
    ("XS"                       _ |XS-d|           ) ; 13
    ("RESULT"                   _ |RESULT-d|       ) ; 35
    ("WORK"                     _ |WORK-d|         ) ; 13
    ("P"                        _ |P-d|            ) ;  1
    ("P1"                       _ |P1-d|           ) ;  1
    ("P2"                       _ |P2-d|           ) ;  1
    ("RESULTn_1"                _ |RESULTn_1-d|    ) ;  1
    ("RESULTn_2"                _ |RESULTn_2-d|    ) ;  1
    ("CNTP"                     _ |CNTP-d|         ) ;  3
    ("CNTS"                     _ |CNTS-d|         ) ;  4
    ("CNTM"                     _ |CNTM-d|         ) ;  6
    ("CNTD"                     _ |CNTD-d|         ) ;  6
    ("n_1499"                   _ |DONE-d|         ) ;  1
    ("ENwork_trig0"             _ |ENwork_trig0-d| ) ;  1
    ("ENwork_trig1"             _ |ENwork_trig1-d| ) ;  1
    ("ENshift_trig0"            _ |ENshift_trig0-d|) ;  1
    ("ENshift_trig1"            _ |ENshift_trig1-d|) ;  1
    ("ENshift_trig2"            _ |ENshift_trig2-d|) ;  1
    ("ENshift_trig3"            _ |ENshift_trig3-d|) ;  1
    ("ENshift_trig4"            _ |ENshift_trig4-d|) ;  1
    ("EN_trig"                  _ |EN_trig-d|      ));  1
;  :outputs
  )

(defsvtv polinom-negedge
  :mod *polinom_out*
  :inputs
  '(("CLK"     1 0)
    ("RST"     0 0)
    ("K1BIT"   |K1BIT| _)
    ("K2BIT"   |K2BIT| _)
    ("K3BIT"   |K3BIT| _)
    ("K4BIT"   |K4BIT| _)
    ("K5BIT"   |K5BIT| _)
    ("SBIT"    |SBIT| _)
    ("INF"     |INF| _)
    ("T"       |TT| _)
    ("ENwork"  |ENwork| _)
    ("ENshift" |ENshift| _)
    )
  :overrides
  '(("RESULTout"                |RESULTout-s|     _) ; 12
    ("XS"                       |XS-s|            _) ; 13
    ("RESULT"                   |RESULT-s|        _) ; 35
    ("WORK"                     |WORK-s|          _) ; 13
    ("P"                        |P-s|             _) ;  1
    ("P1"                       |P1-s|            _) ;  1
    ("P2"                       |P2-s|            _) ;  1
    ("RESULTn_1"                |RESULTn_1-s|     _) ;  1
    ("RESULTn_2"                |RESULTn_2-s|     _) ;  1
    ("CNTP"                     |CNTP-s|          _) ;  3
    ("CNTS"                     |CNTS-s|          _) ;  4
    ("CNTM"                     |CNTM-s|          _) ;  6
    ("CNTD"                     |CNTD-s|          _) ;  6
    ("n_1499"                   |DONE-s|          _) ;  1
    ("ENwork_trig0"             |ENwork_trig0-s|  _) ;  1
    ("ENwork_trig1"             |ENwork_trig1-s|  _) ;  1
    ("ENshift_trig0"            |ENshift_trig0-s| _) ;  1
    ("ENshift_trig1"            |ENshift_trig1-s| _) ;  1
    ("ENshift_trig2"            |ENshift_trig2-s| _) ;  1
    ("ENshift_trig3"            |ENshift_trig3-s| _) ;  1
    ("ENshift_trig4"            |ENshift_trig4-s| _) ;  1
    ("EN_trig"                  |EN_trig-s|       _)) ;  1
  :internals
  '(("RESULTout"                _ |RESULTout-d|    ) ; 12
    ("XS"                       _ |XS-d|           ) ; 13
    ("RESULT"                   _ |RESULT-d|       ) ; 35
    ("WORK"                     _ |WORK-d|         ) ; 13
    ("P"                        _ |P-d|            ) ;  1
    ("P1"                       _ |P1-d|           ) ;  1
    ("P2"                       _ |P2-d|           ) ;  1
    ("RESULTn_1"                _ |RESULTn_1-d|    ) ;  1
    ("RESULTn_2"                _ |RESULTn_2-d|    ) ;  1
    ("CNTP"                     _ |CNTP-d|         ) ;  3
    ("CNTS"                     _ |CNTS-d|         ) ;  4
    ("CNTM"                     _ |CNTM-d|         ) ;  6
    ("CNTD"                     _ |CNTD-d|         ) ;  6
    ("n_1499"                   _ |DONE-d|         ) ;  1
    ("ENwork_trig0"             _ |ENwork_trig0-d| ) ;  1
    ("ENwork_trig1"             _ |ENwork_trig1-d| ) ;  1
    ("ENshift_trig0"            _ |ENshift_trig0-d|) ;  1
    ("ENshift_trig1"            _ |ENshift_trig1-d|) ;  1
    ("ENshift_trig2"            _ |ENshift_trig2-d|) ;  1
    ("ENshift_trig3"            _ |ENshift_trig3-d|) ;  1
    ("ENshift_trig4"            _ |ENshift_trig4-d|) ;  1
    ("EN_trig"                  _ |EN_trig-d|      ));  1
;  :outputs
  )

(defmacro polinom-equalhyps ()
  '(and
    (equal (|POLY-ST->RESULTout|       st) |RESULTout-s|)
    (equal (|POLY-ST->XS|              st) |XS-s|)
    (equal (|POLY-ST->RESULT|          st) |RESULT-s|)
    (equal (|POLY-ST->WORK|            st) |WORK-s|)
    (equal (|POLY-ST->P|               st) |P-s|)
    (equal (|POLY-ST->P1|              st) |P1-s|)
    (equal (|POLY-ST->P2|              st) |P2-s|)
    (equal (|POLY-ST->RESULTn_1|       st) |RESULTn_1-s|)
    (equal (|POLY-ST->RESULTn_2|       st) |RESULTn_2-s|)
    (equal (|POLY-ST->CNTP|            st) |CNTP-s|)
    (equal (|POLY-ST->CNTS|            st) |CNTS-s|)
    (equal (|POLY-ST->CNTM|            st) |CNTM-s|)
    (equal (|POLY-ST->CNTD|            st) |CNTD-s|)
    (equal (|POLY-ST->DONE|            st) |DONE-s|)
    (equal (|POLY-ST->ENwork_trig0|    st) |ENwork_trig0-s|)
    (equal (|POLY-ST->ENwork_trig1|    st) |ENwork_trig1-s|)
    (equal (|POLY-ST->ENshift_trig0|   st) |ENshift_trig0-s|)
    (equal (|POLY-ST->ENshift_trig1|   st) |ENshift_trig1-s|)
    (equal (|POLY-ST->ENshift_trig2|   st) |ENshift_trig2-s|)
    (equal (|POLY-ST->ENshift_trig3|   st) |ENshift_trig3-s|)
    (equal (|POLY-ST->ENshift_trig4|   st) |ENshift_trig4-s|)
    (equal (|POLY-ST->EN_trig|         st) |EN_trig-s|)

    (equal (|POLY-IN->K1BIT|           in) |K1BIT|)
    (equal (|POLY-IN->K2BIT|           in) |K2BIT|)
    (equal (|POLY-IN->K3BIT|           in) |K3BIT|)
    (equal (|POLY-IN->K4BIT|           in) |K4BIT|)
    (equal (|POLY-IN->K5BIT|           in) |K5BIT|)
    (equal (|POLY-IN->SBIT|            in) |SBIT|)
    (equal (|POLY-IN->INF|             in) |INF|)
    (equal (|POLY-IN->TT|              in) |TT|)
    (equal (|POLY-IN->ENwork|          in) |ENwork|)
    (equal (|POLY-IN->ENshift|         in) |ENshift|)))

(local (gl::gl-set-uninterpreted |POLY-ST->RESULTout|))
(local (gl::gl-set-uninterpreted |POLY-ST->XS|))
(local (gl::gl-set-uninterpreted |POLY-ST->RESULT|))
(local (gl::gl-set-uninterpreted |POLY-ST->WORK|))
(local (gl::gl-set-uninterpreted |POLY-ST->P|))
(local (gl::gl-set-uninterpreted |POLY-ST->P1|))
(local (gl::gl-set-uninterpreted |POLY-ST->P2|))
(local (gl::gl-set-uninterpreted |POLY-ST->RESULTn_1|))
(local (gl::gl-set-uninterpreted |POLY-ST->RESULTn_2|))
(local (gl::gl-set-uninterpreted |POLY-ST->CNTP|))
(local (gl::gl-set-uninterpreted |POLY-ST->CNTS|))
(local (gl::gl-set-uninterpreted |POLY-ST->CNTM|))
(local (gl::gl-set-uninterpreted |POLY-ST->CNTD|))
(local (gl::gl-set-uninterpreted |POLY-ST->DONE|))
(local (gl::gl-set-uninterpreted |POLY-ST->ENwork_trig0|))
(local (gl::gl-set-uninterpreted |POLY-ST->ENwork_trig1|))
(local (gl::gl-set-uninterpreted |POLY-ST->ENshift_trig0|))
(local (gl::gl-set-uninterpreted |POLY-ST->ENshift_trig1|))
(local (gl::gl-set-uninterpreted |POLY-ST->ENshift_trig2|))
(local (gl::gl-set-uninterpreted |POLY-ST->ENshift_trig3|))
(local (gl::gl-set-uninterpreted |POLY-ST->ENshift_trig4|))
(local (gl::gl-set-uninterpreted |POLY-ST->EN_trig|))

(local (gl::gl-set-uninterpreted |POLY-IN->K1BIT|))
(local (gl::gl-set-uninterpreted |POLY-IN->K2BIT|))
(local (gl::gl-set-uninterpreted |POLY-IN->K3BIT|))
(local (gl::gl-set-uninterpreted |POLY-IN->K4BIT|))
(local (gl::gl-set-uninterpreted |POLY-IN->K5BIT|))
(local (gl::gl-set-uninterpreted |POLY-IN->SBIT|))
(local (gl::gl-set-uninterpreted |POLY-IN->TT|))
(local (gl::gl-set-uninterpreted |POLY-IN->INF|))
(local (gl::gl-set-uninterpreted |POLY-IN->ENwork|))
(local (gl::gl-set-uninterpreted |POLY-IN->ENshift|))

(local
 (def-gl-rule posedge-lemma-gl
   :hyp (and (polinom-posedge-autohyps)
             (polinom-equalhyps))
   :concl
   (let ((outs (svtv-run (polinom-posedge) (polinom-posedge-autoins))))
     (and
      (equal (cdr (assoc '|RESULTout-d| outs))
             (|RESULTout-NEXT| st))
      (equal (cdr (assoc '|XS-d| outs))
             (|XS-NEXT| st in))
      (equal (cdr (assoc '|RESULT-d| outs))
             (|RESULT-NEXT| st in))
      (equal (cdr (assoc '|WORK-d| outs))
             (|WORK-NEXT| st in))
      (equal (cdr (assoc '|P-d| outs))
             (|P-NEXT| st))
      (equal (cdr (assoc '|P1-d| outs))
             (|P1-NEXT| st in nil))
      (equal (cdr (assoc '|P2-d| outs))
             (|P2-NEXT| st in nil))
      (equal (cdr (assoc '|RESULTn_1-d| outs))
             (|RESULTn_1-NEXT| st))
      (equal (cdr (assoc '|RESULTn_2-d| outs)) (|RESULTn_2-NEXT| st))
      (equal (cdr (assoc '|CNTP-d| outs))
             (|POLY-ST->CNTP| st))
      (equal (cdr (assoc '|CNTS-d| outs))
             (|POLY-ST->CNTS| st))
      (equal (cdr (assoc '|CNTM-d| outs))
             (|POLY-ST->CNTM| st))
      (equal (cdr (assoc '|CNTD-d| outs))
             (|POLY-ST->CNTD| st))
      (equal (cdr (assoc '|DONE-d| outs))
             (|POLY-ST->DONE| st))
      (equal (cdr (assoc '|ENwork_trig0-d| outs))
             (|POLY-IN->ENwork| in))
      (equal (cdr (assoc '|ENwork_trig1-d| outs))
             (|POLY-ST->ENwork_trig0| st))
      (equal (cdr (assoc '|ENshift_trig0-d| outs))
             (|POLY-IN->ENshift| in))
      (equal (cdr (assoc '|ENshift_trig1-d| outs))
             (|POLY-ST->ENshift_trig0| st))
      (equal (cdr (assoc '|ENshift_trig2-d| outs))
             (|POLY-ST->ENshift_trig1| st))
      (equal (cdr (assoc '|ENshift_trig3-d| outs))
             (|POLY-ST->ENshift_trig2| st))
      (equal (cdr (assoc '|ENshift_trig4-d| outs))
             (|POLY-ST->ENshift_trig3| st))
      (equal (cdr (assoc '|EN_trig-d| outs))
             (|EN| st))))
   :g-bindings (polinom-posedge-autobinds)
   :rule-classes ()))

(local
 (define dummy
   ((in poly-in-p))
   (declare (ignore in))
   t))

(local
 (def-gl-rule negedge-lemma-gl
   :hyp (and (polinom-negedge-autohyps)
             (polinom-equalhyps))
   :concl
   (let ((outs (svtv-run (polinom-negedge) (polinom-negedge-autoins))))
     (and
      (equal (cdr (assoc '|RESULTout-d| outs))
             (|POLY-ST->RESULTout| st))
      (equal (cdr (assoc '|XS-d| outs))
             (|POLY-ST->XS| st))
      (equal (cdr (assoc '|RESULT-d| outs))
             (|POLY-ST->RESULT| st))
      (equal (cdr (assoc '|WORK-d| outs))
             (|POLY-ST->WORK| st))
      (equal (cdr (assoc '|P-d| outs))
             (|POLY-ST->P| st))
      (equal (cdr (assoc '|P1-d| outs))
             (|POLY-ST->P1| st))
      (equal (cdr (assoc '|P2-d| outs))
             (|POLY-ST->P2| st))
      (equal (cdr (assoc '|RESULTn_1-d| outs))
             (|POLY-ST->RESULTn_1| st))
      (equal (cdr (assoc '|RESULTn_2-d| outs))
             (|POLY-ST->RESULTn_2| st))
      (equal (cdr (assoc '|CNTP-d| outs))
             (|NEXT-CNTP| st))
      (equal (cdr (assoc '|CNTS-d| outs))
             (|NEXT-CNTS| st))
      (equal (cdr (assoc '|CNTM-d| outs))
             (|NEXT-CNTM| st))
      (equal (cdr (assoc '|CNTD-d| outs))
             (|NEXT-CNTD| st))
      (equal (cdr (assoc '|DONE-d| outs))
             (|NEXT-DONE| st))
      (equal (cdr (assoc '|ENwork_trig0-d| outs))
             (|POLY-ST->ENwork_trig0| st))
      (equal (cdr (assoc '|ENwork_trig1-d| outs))
             (|POLY-ST->ENwork_trig1| st))
      (equal (cdr (assoc '|ENshift_trig0-d| outs))
             (|POLY-ST->ENshift_trig0| st))
      (equal (cdr (assoc '|ENshift_trig1-d| outs))
             (|POLY-ST->ENshift_trig1| st))
      (equal (cdr (assoc '|ENshift_trig2-d| outs))
             (|POLY-ST->ENshift_trig2| st))
      (equal (cdr (assoc '|ENshift_trig3-d| outs))
             (|POLY-ST->ENshift_trig3| st))
      (equal (cdr (assoc '|ENshift_trig4-d| outs))
             (|POLY-ST->ENshift_trig4| st))
      (equal (cdr (assoc '|EN_trig-d| outs))
             (|POLY-ST->EN_trig| st))
      (dummy in)))
   :g-bindings (polinom-negedge-autobinds)
   :rule-classes ()))

(defruled posedge-thm
  (let ((outs (svtv-run
               (polinom-posedge)
               `((|RESULTout-s|     . ,(|POLY-ST->RESULTout|       st))
                 (|XS-s|            . ,(|POLY-ST->XS|              st))
                 (|RESULT-s|        . ,(|POLY-ST->RESULT|          st))
                 (|WORK-s|          . ,(|POLY-ST->WORK|            st))
                 (|P-s|             . ,(|POLY-ST->P|               st))
                 (|P1-s|            . ,(|POLY-ST->P1|              st))
                 (|P2-s|            . ,(|POLY-ST->P2|              st))
                 (|RESULTn_1-s|     . ,(|POLY-ST->RESULTn_1|       st))
                 (|RESULTn_2-s|     . ,(|POLY-ST->RESULTn_2|       st))
                 (|CNTP-s|          . ,(|POLY-ST->CNTP|            st))
                 (|CNTS-s|          . ,(|POLY-ST->CNTS|            st))
                 (|CNTM-s|          . ,(|POLY-ST->CNTM|            st))
                 (|CNTD-s|          . ,(|POLY-ST->CNTD|            st))
                 (|DONE-s|          . ,(|POLY-ST->DONE|            st))
                 (|ENwork_trig0-s|  . ,(|POLY-ST->ENwork_trig0|    st))
                 (|ENwork_trig1-s|  . ,(|POLY-ST->ENwork_trig1|    st))
                 (|ENshift_trig0-s| . ,(|POLY-ST->ENshift_trig0|   st))
                 (|ENshift_trig1-s| . ,(|POLY-ST->ENshift_trig1|   st))
                 (|ENshift_trig2-s| . ,(|POLY-ST->ENshift_trig2|   st))
                 (|ENshift_trig3-s| . ,(|POLY-ST->ENshift_trig3|   st))
                 (|ENshift_trig4-s| . ,(|POLY-ST->ENshift_trig4|   st))
                 (|EN_trig-s|       . ,(|POLY-ST->EN_trig|         st))

                 (|K1BIT|           . ,(|POLY-IN->K1BIT|           in))
                 (|K2BIT|           . ,(|POLY-IN->K2BIT|           in))
                 (|K3BIT|           . ,(|POLY-IN->K3BIT|           in))
                 (|K4BIT|           . ,(|POLY-IN->K4BIT|           in))
                 (|K5BIT|           . ,(|POLY-IN->K5BIT|           in))
                 (|SBIT|            . ,(|POLY-IN->SBIT|            in))
                 (|INF|             . ,(|POLY-IN->INF|             in))
                 (|TT|              . ,(|POLY-IN->TT|              in))
                 (|ENwork|          . ,(|POLY-IN->ENwork|          in))
                 (|ENshift|         . ,(|POLY-IN->ENshift|         in)))))
        (nst (posedge st in nil)))
    (equal
     nst
     (make-poly-st
      :|RESULTout|     (cdr (assoc '|RESULTout-d| outs))
      :|XS|            (cdr (assoc '|XS-d| outs))
      :|RESULT|        (cdr (assoc '|RESULT-d| outs))
      :|WORK|          (cdr (assoc '|WORK-d| outs))
      :|P|             (cdr (assoc '|P-d| outs))
      :|P1|            (cdr (assoc '|P1-d| outs))
      :|P2|            (cdr (assoc '|P2-d| outs))
      :|RESULTn_1|     (cdr (assoc '|RESULTn_1-d| outs))
      :|RESULTn_2|     (cdr (assoc '|RESULTn_2-d| outs))
      :|CNTP|          (cdr (assoc '|CNTP-d| outs))
      :|CNTS|          (cdr (assoc '|CNTS-d| outs))
      :|CNTM|          (cdr (assoc '|CNTM-d| outs))
      :|CNTD|          (cdr (assoc '|CNTD-d| outs))
      :|DONE|          (cdr (assoc '|DONE-d| outs))
      :|ENwork_trig0|  (cdr (assoc '|ENwork_trig0-d| outs))
      :|ENwork_trig1|  (cdr (assoc '|ENwork_trig1-d| outs))
      :|ENshift_trig0| (cdr (assoc '|ENshift_trig0-d| outs))
      :|ENshift_trig1| (cdr (assoc '|ENshift_trig1-d| outs))
      :|ENshift_trig2| (cdr (assoc '|ENshift_trig2-d| outs))
      :|ENshift_trig3| (cdr (assoc '|ENshift_trig3-d| outs))
      :|ENshift_trig4| (cdr (assoc '|ENshift_trig4-d| outs))
      :|EN_trig|       (cdr (assoc '|EN_trig-d| outs)))))
  :enable (polinom-posedge-autohyps-fn
           polinom-posedge-autoins-fn
           posedge)
  :disable unsigned-byte-p
  :use (:instance
        posedge-lemma-gl
        (|RESULTout-s|      (|POLY-ST->RESULTout|       st))
        (|XS-s|             (|POLY-ST->XS|              st))
        (|RESULT-s|         (|POLY-ST->RESULT|          st))
        (|WORK-s|           (|POLY-ST->WORK|            st))
        (|P-s|              (|POLY-ST->P|               st))
        (|P1-s|             (|POLY-ST->P1|              st))
        (|P2-s|             (|POLY-ST->P2|              st))
        (|RESULTn_1-s|      (|POLY-ST->RESULTn_1|       st))
        (|RESULTn_2-s|      (|POLY-ST->RESULTn_2|       st))
        (|CNTP-s|           (|POLY-ST->CNTP|            st))
        (|CNTS-s|           (|POLY-ST->CNTS|            st))
        (|CNTM-s|           (|POLY-ST->CNTM|            st))
        (|CNTD-s|           (|POLY-ST->CNTD|            st))
        (|DONE-s|           (|POLY-ST->DONE|            st))
        (|ENwork_trig0-s|   (|POLY-ST->ENwork_trig0|    st))
        (|ENwork_trig1-s|   (|POLY-ST->ENwork_trig1|    st))
        (|ENshift_trig0-s|  (|POLY-ST->ENshift_trig0|   st))
        (|ENshift_trig1-s|  (|POLY-ST->ENshift_trig1|   st))
        (|ENshift_trig2-s|  (|POLY-ST->ENshift_trig2|   st))
        (|ENshift_trig3-s|  (|POLY-ST->ENshift_trig3|   st))
        (|ENshift_trig4-s|  (|POLY-ST->ENshift_trig4|   st))
        (|EN_trig-s|        (|POLY-ST->EN_trig|         st))

        (|K1BIT|            (|POLY-IN->K1BIT|           in))
        (|K2BIT|            (|POLY-IN->K2BIT|           in))
        (|K3BIT|            (|POLY-IN->K3BIT|           in))
        (|K4BIT|            (|POLY-IN->K4BIT|           in))
        (|K5BIT|            (|POLY-IN->K5BIT|           in))
        (|SBIT|             (|POLY-IN->SBIT|            in))
        (|INF|              (|POLY-IN->INF|             in))
        (|TT|               (|POLY-IN->TT|              in))
        (|ENwork|           (|POLY-IN->ENwork|          in))
        (|ENshift|          (|POLY-IN->ENshift|         in))))

(defruled negedge-thm
  (let ((outs (svtv-run
               (polinom-negedge)
               `((|RESULTout-s|     . ,(|POLY-ST->RESULTout|       st))
                 (|XS-s|            . ,(|POLY-ST->XS|              st))
                 (|RESULT-s|        . ,(|POLY-ST->RESULT|          st))
                 (|WORK-s|          . ,(|POLY-ST->WORK|            st))
                 (|P-s|             . ,(|POLY-ST->P|               st))
                 (|P1-s|            . ,(|POLY-ST->P1|              st))
                 (|P2-s|            . ,(|POLY-ST->P2|              st))
                 (|RESULTn_1-s|     . ,(|POLY-ST->RESULTn_1|       st))
                 (|RESULTn_2-s|     . ,(|POLY-ST->RESULTn_2|       st))
                 (|CNTP-s|          . ,(|POLY-ST->CNTP|            st))
                 (|CNTS-s|          . ,(|POLY-ST->CNTS|            st))
                 (|CNTM-s|          . ,(|POLY-ST->CNTM|            st))
                 (|CNTD-s|          . ,(|POLY-ST->CNTD|            st))
                 (|DONE-s|          . ,(|POLY-ST->DONE|            st))
                 (|ENwork_trig0-s|  . ,(|POLY-ST->ENwork_trig0|    st))
                 (|ENwork_trig1-s|  . ,(|POLY-ST->ENwork_trig1|    st))
                 (|ENshift_trig0-s| . ,(|POLY-ST->ENshift_trig0|   st))
                 (|ENshift_trig1-s| . ,(|POLY-ST->ENshift_trig1|   st))
                 (|ENshift_trig2-s| . ,(|POLY-ST->ENshift_trig2|   st))
                 (|ENshift_trig3-s| . ,(|POLY-ST->ENshift_trig3|   st))
                 (|ENshift_trig4-s| . ,(|POLY-ST->ENshift_trig4|   st))
                 (|EN_trig-s|       . ,(|POLY-ST->EN_trig|         st))

                 (|K1BIT|           . ,(|POLY-IN->K1BIT|           in))
                 (|K2BIT|           . ,(|POLY-IN->K2BIT|           in))
                 (|K3BIT|           . ,(|POLY-IN->K3BIT|           in))
                 (|K4BIT|           . ,(|POLY-IN->K4BIT|           in))
                 (|K5BIT|           . ,(|POLY-IN->K5BIT|           in))
                 (|SBIT|            . ,(|POLY-IN->SBIT|            in))
                 (|INF|             . ,(|POLY-IN->INF|             in))
                 (|TT|              . ,(|POLY-IN->TT|              in))
                 (|ENwork|          . ,(|POLY-IN->ENwork|          in))
                 (|ENshift|         . ,(|POLY-IN->ENshift|         in))))))
    (equal
     (negedge st)
     (make-poly-st
      :|RESULTout|     (cdr (assoc '|RESULTout-d| outs))
      :|XS|            (cdr (assoc '|XS-d| outs))
      :|RESULT|        (cdr (assoc '|RESULT-d| outs))
      :|WORK|          (cdr (assoc '|WORK-d| outs))
      :|P|             (cdr (assoc '|P-d| outs))
      :|P1|            (cdr (assoc '|P1-d| outs))
      :|P2|            (cdr (assoc '|P2-d| outs))
      :|RESULTn_1|     (cdr (assoc '|RESULTn_1-d| outs))
      :|RESULTn_2|     (cdr (assoc '|RESULTn_2-d| outs))
      :|CNTP|          (cdr (assoc '|CNTP-d| outs))
      :|CNTS|          (cdr (assoc '|CNTS-d| outs))
      :|CNTM|          (cdr (assoc '|CNTM-d| outs))
      :|CNTD|          (cdr (assoc '|CNTD-d| outs))
      :|DONE|          (cdr (assoc '|DONE-d| outs))
      :|ENwork_trig0|  (cdr (assoc '|ENwork_trig0-d| outs))
      :|ENwork_trig1|  (cdr (assoc '|ENwork_trig1-d| outs))
      :|ENshift_trig0| (cdr (assoc '|ENshift_trig0-d| outs))
      :|ENshift_trig1| (cdr (assoc '|ENshift_trig1-d| outs))
      :|ENshift_trig2| (cdr (assoc '|ENshift_trig2-d| outs))
      :|ENshift_trig3| (cdr (assoc '|ENshift_trig3-d| outs))
      :|ENshift_trig4| (cdr (assoc '|ENshift_trig4-d| outs))
      :|EN_trig|       (cdr (assoc '|EN_trig-d| outs)))))
  :enable (polinom-negedge-autohyps-fn
           polinom-negedge-autoins-fn
           negedge)
  :disable unsigned-byte-p
  :use (:instance
        negedge-lemma-gl
        (|RESULTout-s|      (|POLY-ST->RESULTout|       st))
        (|XS-s|             (|POLY-ST->XS|              st))
        (|RESULT-s|         (|POLY-ST->RESULT|          st))
        (|WORK-s|           (|POLY-ST->WORK|            st))
        (|P-s|              (|POLY-ST->P|               st))
        (|P1-s|             (|POLY-ST->P1|              st))
        (|P2-s|             (|POLY-ST->P2|              st))
        (|RESULTn_1-s|      (|POLY-ST->RESULTn_1|       st))
        (|RESULTn_2-s|      (|POLY-ST->RESULTn_2|       st))
        (|CNTP-s|           (|POLY-ST->CNTP|            st))
        (|CNTS-s|           (|POLY-ST->CNTS|            st))
        (|CNTM-s|           (|POLY-ST->CNTM|            st))
        (|CNTD-s|           (|POLY-ST->CNTD|            st))
        (|DONE-s|           (|POLY-ST->DONE|            st))
        (|ENwork_trig0-s|   (|POLY-ST->ENwork_trig0|    st))
        (|ENwork_trig1-s|   (|POLY-ST->ENwork_trig1|    st))
        (|ENshift_trig0-s|  (|POLY-ST->ENshift_trig0|   st))
        (|ENshift_trig1-s|  (|POLY-ST->ENshift_trig1|   st))
        (|ENshift_trig2-s|  (|POLY-ST->ENshift_trig2|   st))
        (|ENshift_trig3-s|  (|POLY-ST->ENshift_trig3|   st))
        (|ENshift_trig4-s|  (|POLY-ST->ENshift_trig4|   st))
        (|EN_trig-s|        (|POLY-ST->EN_trig|         st))

        (|K1BIT|            (|POLY-IN->K1BIT|           in))
        (|K2BIT|            (|POLY-IN->K2BIT|           in))
        (|K3BIT|            (|POLY-IN->K3BIT|           in))
        (|K4BIT|            (|POLY-IN->K4BIT|           in))
        (|K5BIT|            (|POLY-IN->K5BIT|           in))
        (|SBIT|             (|POLY-IN->SBIT|            in))
        (|INF|              (|POLY-IN->INF|             in))
        (|TT|               (|POLY-IN->TT|              in))
        (|ENwork|           (|POLY-IN->ENwork|          in))
        (|ENshift|          (|POLY-IN->ENshift|         in))))
