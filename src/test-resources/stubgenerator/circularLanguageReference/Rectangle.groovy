class Rectangle implements Shape {
    private double x, y

    Rectangle(double x, double y) {
        this.x = x
        this.y = y
    }

    double area() {
        x * y
    }
}