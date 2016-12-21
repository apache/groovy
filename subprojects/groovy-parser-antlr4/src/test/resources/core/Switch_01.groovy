switch (a) {
    case 1:
        break;
    case 2:
        break;
}

switch (a) {
    case 1:
        break;
    case 2:
        break;
    default:
        break;
}

switch (a) {
    case 1:
    case 2:
        break;
    case 3:
        break;
    default:
        break;
}

switch (a) {
    case 1:
    case 2 + 3:
        break;
    case 3:
        break;
    case 4 + 2:
    case 5:
    default:
        break;
}

switch (a) {
    case 1:
    case 2 + 3: break;
    case 3: break;
    case 4 + 2:
    case 5:
    default: break;
}

switch (a) {case 1:
    case 2 + 3: break;
    case 3: break;
    case 4 + 2:
    case 5:
    default: break;}

switch (a) {
    case 1:
        int x = 1;
    default:
        int y = 2;
}
