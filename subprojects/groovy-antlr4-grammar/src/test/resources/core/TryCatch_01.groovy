import java.sql.SQLException

try {
    assert true;
} finally {
    return 0;
}

try {
    assert true;
} catch(Exception e) {
    assert false;
}

try {
    assert true;
} catch(Exception
            e) {
    assert false;
}

try {
    assert true;
} catch(e) {
    assert false;
}

try {
    assert true;
} catch(e) {
    assert false;
} catch(t) {
    assert false;
}

try {
    assert true;
} catch(final e) {
    assert false;
}

try {
    assert true;
} catch(final Exception e) {
    assert false;
}

try {
    assert true;
} catch(IOException e) {
    assert false;
} catch(Exception e) {
    assert false;
}

try {
    assert true;
} catch(IOException e) {
    assert false;
} catch(Exception e) {
    assert false;
} finally {
    return 0;
}

try
{
    assert true;
}
catch(IOException e)
{
    assert false;
}
catch(Exception e)
{
    assert false;
}
finally
{
    return 0;
}

try {
    assert true;
} catch(Exception e) {
    assert false;
} finally {
    return 0;
}

try {
    assert true;
} catch(NullPointerException | IOException e) {
    assert false;
} finally {
    return 0;
}

try {
    assert true;
} catch(NullPointerException | IOException e) {
    assert false;
}

try {
    assert true;
} catch(NullPointerException |
        IOException |
        SQLException
                e) {
    assert false;
}
