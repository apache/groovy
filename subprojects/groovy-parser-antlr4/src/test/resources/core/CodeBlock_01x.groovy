def m() {
    int result = 0;
    {
        int i = 1;
        result += i;
    }
    {
        int i = 2;
        result += i;

        {
            int j = 3;
            result += j;
        }
        {
            int j = 4;
            result += j;
        }

    }
    return { result }
}

assert m()() == 10