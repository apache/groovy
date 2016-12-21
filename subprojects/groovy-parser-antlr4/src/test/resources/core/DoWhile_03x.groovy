int i, j = 0;

do {
    i++
    if (i == 4) break;

    if (j == 3) continue;
    j++
} while (true)

assert j == 3
