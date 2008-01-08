import java.text.DecimalFormat

// ---------------------------------------------------------------------
// The Great Computer Language Shootout
// http://shootout.alioth.debian.org/
//
// Use JRE 1.4 features only [JRE 1.5 'printf' would have helped better
// streamline code]
//
// Contributed by Anthony Borla
// Modified by Alex Tkachman
// ---------------------------------------------------------------------

def calculate()
{
  n = Integer.parseInt(args[0])

  double s0 = s1 = s2 = s3 = s4 = s5 = s6 = s7 = s8 = 0.0D ; alt = true ; double d = 1.0D

  double twoThird = 2.0D / 3.0D
  while (d <= n)
  {
    double d2 = d * d, d3 = d2 * d, ds = Math.sin(d), ds2 = ds * ds, dc2 = 1.0D - ds2

    final double ONE = 1.0d
      def dminus1 = d - ONE
    s0 = s0 * twoThird + ONE
    s1 += ONE / Math.sqrt(d)
    double oneByD = ONE / d
    s2 += oneByD / (d + ONE)
    double oneByD3 = ONE / d3
    s3 += oneByD3 / ds2
    s4 += oneByD3 / dc2
    s5 += oneByD
    s6 += ONE / d2
    if (alt) {
      s7 += oneByD
      s8 += ONE / (d + dminus1)
    }
    else {
      s7 -= oneByD
      s8 -= ONE / (d + dminus1)
    }

    alt = !alt; 
    d += ONE
  }

  fmt = new DecimalFormat("##0.000000000")

  result = fmt.format(s0) ; println "${result}\t(2/3)^k"
  result = fmt.format(s1) ; println "${result}\tk^-0.5"
  result = fmt.format(s2) ; println "${result}\t1/k(k+1)"
  result = fmt.format(s3) ; println "${result}\tFlint Hills"
  result = fmt.format(s4) ; println "${result}\tCookson Hills"
  result = fmt.format(s5) ; println "${result}\tHarmonic"
  result = fmt.format(s6) ; println "${result}\tRiemann Zeta"
  result = fmt.format(s7) ; println "${result}\tAlternating Harmonic"
  result = fmt.format(s8) ; println "${result}\tGregory"
}

// --------------------------------

long start = System.currentTimeMillis ()
calculate()
println "${System.currentTimeMillis () - start}ms"

