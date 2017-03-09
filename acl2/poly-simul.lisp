(in-package "ACL2")
(include-book "poly-state")

(local (include-book "centaur/bitops/ihsext-basics" :dir :system))


(fty::deflist
 poly-in-list
 :elt-type poly-in
 :true-listp t)

(defrule deflist-repeat
  (implies (poly-in-p in)
           (poly-in-list-p (repeat n in)))
  :enable repeat)

(define poly-run
  ((st poly-st-p)
   (inl poly-in-list-p)
   (fixbug booleanp))
  :returns (st poly-st-p :hyp (poly-st-p st))
  (if (endp inl)
      st
    (poly-run (clk st (car inl) fixbug) (cdr inl) fixbug)))

(define repeat-7
  ((in poly-in-p))
  :returns (inl poly-in-list-p :hyp :guard)
  (repeat
   (+ 2 (* 13 (+ 35 36 35 45 45 45 49)))
   in))

(define repeat-6
  ((in poly-in-p))
  :returns (inl poly-in-list-p :hyp :guard)
  (repeat
   (+ 2 (* 13 (+ 35 36 35 45 45 45)))
   in))

(define repeat-5
  ((in poly-in-p))
  :returns (inl poly-in-list-p :hyp :guard)
  (repeat
   (+ 2 (* 13 (+ 35 36 35 45 45)))
   in))

(define repeat-4
  ((in poly-in-p))
  :returns (inl poly-in-list-p :hyp :guard)
  (repeat
   (+ 2 (* 13 (+ 35 36 35 45)))
   in))

(define repeat-3
  ((in poly-in-p))
  :returns (inl poly-in-list-p :hyp :guard)
  (repeat
   (+ 2 (* 13 (+ 35 36 35)))
   in))

(define repeat-2
  ((in poly-in-p))
  :returns (inl poly-in-list-p :hyp :guard)
  (repeat
   (+ 2 (* 13 (+ 35 36)))
   in))

(define repeat-1
  ((in poly-in-p))
  :returns (inl poly-in-list-p :hyp :guard)
  (repeat
   (+ 2 (* 13 (+ 35)))
   in))

(define repeat-0
  ((in poly-in-p))
  :returns (inl poly-in-list-p :hyp :guard)
  (repeat
   (+ 2 (* 13 (+)))
   in))

(define RESULTout-full
  ((in poly-in-p)
   (fixbug booleanp))
  :returns (RESULTout unsigned-12-p)
  (|RESULTout-NEXT|
   (poly-run (reset) (repeat-7 in) fixbug)))

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

(define fun-7
  ((st poly-st-p))
  :returns (RESULTout unsigned-12-p)
  (clip (|POLY-ST->RESULT| st)))

(define fun-6
  ((st poly-st-p)
   (fixbug booleanp))
  :returns (RESULTout unsigned-12-p)
  (let ((res (logext 35 (|POLY-ST->RESULT| st)))
        (xs (logext 13 (|POLY-ST->XS| st)))
        (p1 (if fixbug 0 (|POLY-ST->P1| st)))
        (p2 (if fixbug 0 (|POLY-ST->P2| st))))
    (clip
     (loghead
      35
      (+ 1032
         (ash (+ (* res xs) p1 p2 (expt 2 13)) -14))))))

(define RESULTout-6
  ((in poly-in-p)
   (fixbug booleanp))
  :returns (RESULTout unsigned-12-p)
  (fun-6
   (poly-run (reset) (repeat-6 in) fixbug)
   fixbug))
#|
(let* ((in (make-poly-in
            :|CLK|          0
            :|RST|          0
            :|K1BIT|        127
            :|K2BIT|        63
            :|K3BIT|        15
            :|K4BIT|        15
            :|K5BIT|        7
            :|SBIT|         15
           :|INF|          31
           :|TT|           1100
           :|ENwork|       1
           :|ENshift|      0))
       (fixbug nil)
       (st (poly-run (reset) (repeat-6 in) fixbug))
       (res (logext 35 (|POLY-ST->RESULT| st)))
       (xs (logext 13 (|POLY-ST->XS| st)))
       (p1 (|POLY-ST->P1| st))
       (p2 (|POLY-ST->P1| st))
       )
  (list
   (cons 'RESULT res)
   (cons 'XS     xs)
   (cons 'P1     p1)
   (cons 'P2     p2)
   (cons '|RESULT*XS| (* res xs))
   (cons '|RESULT*XS>>14| (ash (* res xs) -14))
   (cons 'good (RESULTout-full in fixbug))
   )

  )



(let ((in (make-poly-in
           :|CLK|          0
           :|RST|          0
           :|K1BIT|        127
           :|K2BIT|        63
           :|K3BIT|        15
           :|K4BIT|        15
           :|K5BIT|        7
           :|SBIT|         15
           :|INF|          31
           :|TT|           3000
           :|ENwork|       1
           :|ENshift|      0))
      (fixbug nil))
  (cons
   (RESULTout-full in fixbug)
   (RESULTout-6    in fixbug)))
|#


(rule
 (equal
  (|POLY-ST->DONE|
   (poly-run
    (reset)
    (repeat
;  (+ 2 (* 13 (+ 35 36 35 45 45 45 35)) 0)
     (+ 2 (* 13 (+ 35 36 35 45 45 45 49)) 0)
     (make-poly-in
      :|CLK|          0
      :|RST|          0
      :|K1BIT|        127
      :|K2BIT|        63
      :|K3BIT|        15
      :|K4BIT|        15
      :|K5BIT|        7
      :|SBIT|         15
      :|INF|          31
      :|TT|           2
      :|ENwork|       1
      :|ENshift|      0))
    nil))
  1))
