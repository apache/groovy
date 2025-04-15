/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
// ---------------------------------------------------------------------
// The Great Computer Language Shootout
// http://shootout.alioth.debian.org/
//
// Contributed by Anthony Borla
// ---------------------------------------------------------------------

def calc()
{
  n = Integer.parseInt(args[0])

  printf("Ack(3,%d): %d\n", n, ack(3, n))
  printf("Fib(%.1f): %.1f\n", 27.0D + n, fib(27.0D + n))

  n -= 1
  printf("Tak(%d,%d,%d): %d\n", n * 3, n * 2, n, tak(n * 3, n * 2, n))

  printf("Fib(3): %d\n", fib(3))
  printf("Tak(3.0,2.0,1.0): %.1f\n", tak(3.0D, 2.0D, 1.0D))
}

// --------------------------------

def ack(x, y)
{
  if (x == 0) return y + 1
  if (y == 0) return ack(x - 1, 1)
  return ack(x - 1, ack(x, y - 1))
}

// --------------

def fib(int n)
{
  if (n < 2I) return 1I
  return fib(n - 2I) + fib(n - 1I)
}

def fib(double n)
{
  if (n < 2.0D) return 1.0D
  return fib(n - 2.0D) + fib(n - 1.0D)
}

// --------------

def tak(int x, int y, int z)
{
  if (y < x) return tak(tak(x - 1I, y, z), tak(y - 1I, z, x), tak(z - 1I, x, y))
  return z
}

def tak(double x, double y, double z)
{
  if (y < x) return tak(tak(x - 1.0D, y, z), tak(y - 1.0D, z, x), tak(z - 1.0D, x, y))
  return z
}

calc ()