# Traveling Salesman


## Compile & Run

Recursively create directory and parents
```bash
mkdir -p build/classes
```

Compile java files into classes directory
```bash
javac -d build/classes src/*.java
```

Run Branch & Bound algorithm
```bash
java -cp build/classes BranchBound
```
