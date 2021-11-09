## `doctor`

Doctor (*documentation-generator*) accepts an input containing a list of AST nodes, and generates a Javadoc-like documentation template.

### Dependencies
- JDK 11+
- [Maven](https://maven.apache.org/)

### Building and running

- `mvn clean install` creates `/target/<doctor-version-jar-with-dependencies>.jar`
- Options:
```
  --source: Generate output as source files
  ```
- pipe-in input from [ast-filter](https://github.com/Deee92/ast-filter)
    - `filter -f method -v public | doctor --source`

#### TODO

- write tests
- support documentation-generation for classes
