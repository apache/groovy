package core

import java.util.concurrent.atomic.AtomicInteger

def inc(AtomicInteger x) { x.incrementAndGet() }
def a = new AtomicInteger(0)

inc a
assert 1 == a.get()
