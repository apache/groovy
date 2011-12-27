/* The Computer Language Shootout
   http://shootout.alioth.debian.org/

   contributed by Keenan Tims
   modified by Michael Barker
*/


public class chameneos {

    private MeetingPlace mp;

    public static final Colour[] COLOURS = { Colour.BLUE, Colour.RED, Colour.YELLOW, Colour.BLUE };

    private Creature[] creatures = new Creature[COLOURS.length];

    public enum Colour {
        RED, BLUE, YELLOW, FADED
    }

    public class Creature extends Thread {

        private MeetingPlace mp;
        private Colour colour;
        private int met = 0;
        private Colour other;

        public Creature(Colour c, MeetingPlace mp) {
            this.colour = c;
            this.mp = mp;
        }

        public void run() {
            try {
                while (colour != Colour.FADED) {
                    mp.meet(this);
                    if (other == Colour.FADED)
                        colour = Colour.FADED;
                    else {
                        met++;
                        colour = complement(other);
                    }
                }
            } catch (InterruptedException e) {
                // Let the thread exit.
            }
        }

        private Colour complement(Colour other) {
            if (colour == other)
                return colour;
            switch (colour) {
            case BLUE:
                return other == Colour.RED ? Colour.YELLOW : Colour.RED;
            case RED:
                return other == Colour.BLUE ? Colour.YELLOW : Colour.BLUE;
            case YELLOW:
                return other == Colour.BLUE ? Colour.RED : Colour.BLUE;
            default:
                return colour;
            }
        }

        public int getCreaturesMet() {
            return met;
        }

        public Colour getColour() {
            return colour;
        }

        public void setOther(Colour other) throws InterruptedException {
            this.other = other;
        }
    }

    public class MeetingPlace {

        int n;

        public MeetingPlace(int n) {
            this.n = n;
        }

        Creature other = null;
        public void meet(Creature c) throws InterruptedException {

            synchronized (this) {
                if (n > 0) {
                    if (other == null) {
                        other = c;
                        this.wait();
                    } else {
                        other.setOther(c.getColour());
                        c.setOther(other.getColour());
                        other = null;
                        n--;
                        this.notify();
                    }
                } else {
                    c.setOther(Colour.FADED);
                }
            }
        }
    }

    public chameneos(int n) throws InterruptedException {
        int meetings = 0;
        mp = new MeetingPlace(n);

        for (int i = 0; i < COLOURS.length; i++) {
            creatures[i] = new Creature(COLOURS[i], mp);
            creatures[i].start();
        }

        // wait for all threads to complete
        for (int i = 0; i < COLOURS.length; i++)
            creatures[i].join();

        // sum all the meetings
        for (int i = 0; i < COLOURS.length; i++) {
            meetings += creatures[i].getCreaturesMet();
        }

        System.out.println(meetings);
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1)
            throw new IllegalArgumentException();
        new chameneos(Integer.parseInt(args[0]));
    }
}
