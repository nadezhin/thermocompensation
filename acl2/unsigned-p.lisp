(in-package "ACL2")
(include-book "std/util/define" :dir :system)
(include-book "std/util/defrule" :dir :system)
(include-book "ihs/basic-definitions" :dir :system)
(include-book "centaur/fty/top" :dir :system)

(local (include-book "centaur/bitops/ihsext-basics" :dir :system))
(local (in-theory (disable unsigned-byte-p)))

(defmacro unsigned-1-p (x)
  `(unsigned-byte-p 1 ,x))
(define unsigned-1-p-fix
  ((x unsigned-1-p))
  :returns (r (unsigned-byte-p 1 r))
  (if (mbt (unsigned-1-p x))
      x
    (loghead 1 x))
  ///
  (defrule unsigned-1-p-projection
    (implies (unsigned-1-p x)
             (equal (unsigned-1-p-fix x) x)))
  (defrule unsigned-1-p-idempotent
    (equal (unsigned-1-p-fix (unsigned-1-p-fix x))
           (unsigned-1-p-fix x))))
(fty::deffixtype
 unsigned-1
 :pred unsigned-1-p
 :fix unsigned-1-p-fix
 :equiv unsigned-1-equiv
 :define t)

(defmacro unsigned-3-p (x)
  `(unsigned-byte-p 3 ,x))
(define unsigned-3-p-fix
  ((x unsigned-3-p))
  :returns (r (unsigned-byte-p 3 r))
  (if (mbt (unsigned-3-p x))
      x
    (loghead 3 x))
  ///
  (defrule unsigned-3-p-projection
    (implies (unsigned-3-p x)
             (equal (unsigned-3-p-fix x) x)))
  (defrule unsigned-3-p-idempotent
    (equal (unsigned-3-p-fix (unsigned-3-p-fix x))
           (unsigned-3-p-fix x))))
(fty::deffixtype
 unsigned-3
 :pred unsigned-3-p
 :fix unsigned-3-p-fix
 :equiv unsigned-3-equiv
 :define t)

(defmacro unsigned-4-p (x)
  `(unsigned-byte-p 4 ,x))
(define unsigned-4-p-fix
  ((x unsigned-4-p))
  :returns (r (unsigned-byte-p 4 r))
  (if (mbt (unsigned-4-p x))
      x
    (loghead 4 x))
  ///
  (defrule unsigned-4-p-projection
    (implies (unsigned-4-p x)
             (equal (unsigned-4-p-fix x) x)))
  (defrule unsigned-4-p-idempotent
    (equal (unsigned-4-p-fix (unsigned-4-p-fix x))
           (unsigned-4-p-fix x))))
(fty::deffixtype
 unsigned-4
 :pred unsigned-4-p
 :fix unsigned-4-p-fix
 :equiv unsigned-4-equiv
 :define t)

(defmacro unsigned-5-p (x)
  `(unsigned-byte-p 5 ,x))
(define unsigned-5-p-fix
  ((x unsigned-5-p))
  :returns (r (unsigned-byte-p 5 r))
  (if (mbt (unsigned-5-p x))
      x
    (loghead 5 x))
  ///
  (defrule unsigned-5-p-projection
    (implies (unsigned-5-p x)
             (equal (unsigned-5-p-fix x) x)))
  (defrule unsigned-5-p-idempotent
    (equal (unsigned-5-p-fix (unsigned-5-p-fix x))
           (unsigned-5-p-fix x))))
(fty::deffixtype
 unsigned-5
 :pred unsigned-5-p
 :fix unsigned-5-p-fix
 :equiv unsigned-5-equiv
 :define t)

(defmacro unsigned-6-p (x)
  `(unsigned-byte-p 6 ,x))
(define unsigned-6-p-fix
  ((x unsigned-6-p))
  :returns (r (unsigned-byte-p 6 r))
  (if (mbt (unsigned-6-p x))
      x
    (loghead 6 x))
  ///
  (defrule unsigned-6-p-projection
    (implies (unsigned-6-p x)
             (equal (unsigned-6-p-fix x) x)))
  (defrule unsigned-6-p-idempotent
    (equal (unsigned-6-p-fix (unsigned-6-p-fix x))
           (unsigned-6-p-fix x))))
(fty::deffixtype
 unsigned-6
 :pred unsigned-6-p
 :fix unsigned-6-p-fix
 :equiv unsigned-6-equiv
 :define t)

(defmacro unsigned-7-p (x)
  `(unsigned-byte-p 7 ,x))
(define unsigned-7-p-fix
  ((x unsigned-7-p))
  :returns (r (unsigned-byte-p 7 r))
  (if (mbt (unsigned-7-p x))
      x
    (loghead 7 x))
  ///
  (defrule unsigned-7-p-projection
    (implies (unsigned-7-p x)
             (equal (unsigned-7-p-fix x) x)))
  (defrule unsigned-7-p-idempotent
    (equal (unsigned-7-p-fix (unsigned-7-p-fix x))
           (unsigned-7-p-fix x))))
(fty::deffixtype
 unsigned-7
 :pred unsigned-7-p
 :fix unsigned-7-p-fix
 :equiv unsigned-7-equiv
 :define t)

(defmacro unsigned-8-p (x)
  `(unsigned-byte-p 8 ,x))
(define unsigned-8-p-fix
  ((x unsigned-8-p))
  :returns (r (unsigned-byte-p 8 r))
  (if (mbt (unsigned-8-p x))
      x
    (loghead 8 x))
  ///
  (defrule unsigned-8-p-projection
    (implies (unsigned-8-p x)
             (equal (unsigned-8-p-fix x) x)))
  (defrule unsigned-8-p-idempotent
    (equal (unsigned-8-p-fix (unsigned-8-p-fix x))
           (unsigned-8-p-fix x))))
(fty::deffixtype
 unsigned-8
 :pred unsigned-8-p
 :fix unsigned-8-p-fix
 :equiv unsigned-8-equiv
 :define t)

(defmacro unsigned-12-p (x)
  `(unsigned-byte-p 12 ,x))
(define unsigned-12-p-fix
  ((x unsigned-12-p))
  :returns (r (unsigned-byte-p 12 r))
  (if (mbt (unsigned-12-p x))
      x
    (loghead 12 x))
  ///
  (defrule unsigned-12-p-projection
    (implies (unsigned-12-p x)
             (equal (unsigned-12-p-fix x) x)))
  (defrule unsigned-12-p-idempotent
    (equal (unsigned-12-p-fix (unsigned-12-p-fix x))
           (unsigned-12-p-fix x))))
(fty::deffixtype
 unsigned-12
 :pred unsigned-12-p
 :fix unsigned-12-p-fix
 :equiv unsigned-12-equiv
 :define t)

(defmacro unsigned-13-p (x)
  `(unsigned-byte-p 13 ,x))
(define unsigned-13-p-fix
  ((x unsigned-13-p))
  :returns (r (unsigned-byte-p 13 r))
  (if (mbt (unsigned-13-p x))
      x
    (loghead 13 x))
  ///
  (defrule unsigned-13-p-projection
    (implies (unsigned-13-p x)
             (equal (unsigned-13-p-fix x) x)))
  (defrule unsigned-13-p-idempotent
    (equal (unsigned-13-p-fix (unsigned-13-p-fix x))
           (unsigned-13-p-fix x))))
(fty::deffixtype
 unsigned-13
 :pred unsigned-13-p
 :fix unsigned-13-p-fix
 :equiv unsigned-13-equiv
 :define t)

(defmacro unsigned-35-p (x)
  `(unsigned-byte-p 35 ,x))
(define unsigned-35-p-fix
  ((x unsigned-35-p))
  :returns (r (unsigned-byte-p 35 r))
  (if (mbt (unsigned-35-p x))
      x
    (loghead 35 x))
  ///
  (defrule unsigned-35-p-projection
    (implies (unsigned-35-p x)
             (equal (unsigned-35-p-fix x) x)))
  (defrule unsigned-35-p-idempotent
    (equal (unsigned-35-p-fix (unsigned-35-p-fix x))
           (unsigned-35-p-fix x))))
(fty::deffixtype
 unsigned-35
 :pred unsigned-35-p
 :fix unsigned-35-p-fix
 :equiv unsigned-35-equiv
 :define t)
