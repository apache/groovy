/*
 * The Computer Language Shootout
 * http://shootout.alioth.debian.org/
 *
 * contributed by Jochen Hinrichsen
 */

double Cr, Ci, Tr, Ti, Zr = 0, Zi = 0, limit_sq = 4.0
int res, i = 0, x = 0, y = 0, pos = 0, acc = 1, iter = 50
res = (args.length >= 1) ? Integer.parseInt(args[0], 10) : 200
int max = (res * res) >>> 3
def pbm_data = new byte[max]
String pbm_header = new String('P4' + ((char) 012) + res + ' ' + res + ((char) 012))

System.out.write(pbm_header.getBytes(), 0, pbm_header.length())

while (pos < max) {
    Cr = (2*((double)x++)/res - 1.5)
    Ci = (2*((double)y)/res - 1)

    acc <<= 1
    while (((acc & 1) == 0) && (i++ < iter)) {
        Tr = Zr*Zr - Zi*Zi + Cr
        Ti = 2*Zr*Zi + Ci
        Zr = Tr
        Zi = Ti

        acc |= (Zr*Zr+Zi*Zi > limit_sq) ? 1 : 0
    }

    if (x == res) {
        y++
        if (acc < 256) acc <<= (8 - res % 8)
    }

    if (acc > 255) {
        pbm_data[pos++] = (byte) (acc ^= 255)
        acc = 1
    }

    x %= res
    Zr = Zi = i = 0
}

System.out.write(pbm_data, 0, pos)
