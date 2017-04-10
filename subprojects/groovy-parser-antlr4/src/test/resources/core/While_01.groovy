while(true) assert true

while(
        true
) assert true

while(true)
    assert true

while(true) {
    break;
}

out:
while(true) {
    break out;
}

out1:
while(true) {
    break out1;
    out2: while (true) {
        break out2;
    }
}


while(true) {
    continue
}

out:
while(true) {
    continue out;
}

out1:
while(true) {
    continue out1;
    out2: while (true) {
        continue out2;
    }
}

out1:
while(true) {
    continue out1;
    out2: while (true) {
        break out2;
    }
}


while (false)
    int number = 1

while(true);

