module demo::StringTemplate

import String;
import IO;

// Illustrating of template-based code generation

// Capitalize the first character of a string

public str capitalize(str s) {
  return toUpperCase(substring(s, 0, 1)) + substring(s, 1);
}

// Generate a class with given name and fields.

public str genClass(str name, map[str,str] fields) {
  return "
    public class <name> {
      <for (x <- fields) {>
        private <fields[x]> <x>;
        public void set<capitalize(x)>(<fields[x]> <x>) {
          this.<x> = <x>;
        }
        public <fields[x]> get<capitalize(x)>() {
          return <x>;
        }
      <}>
    }
";
}

// Example of use

private  map[str, str] fields = (
     "name" : "String",
     "age" : "Integer",
     "address" : "String"
  );
  
public void person(){
	println(genClass("Person", fields));
}





  // Beware, in the generated code each empty line contains 6 spaces!
  test genClass("Person", fields) ==
              "
    public class Person {
      
        private Integer age;
        public void setAge(Integer age) {
          this.age = age;
        }
        public Integer getAge() {
          return age;
        }
      
        private String name;
        public void setName(String name) {
          this.name = name;
        }
        public String getName() {
          return name;
        }
      
        private String address;
        public void setAddress(String address) {
          this.address = address;
        }
        public String getAddress() {
          return address;
        }
      
    }
";
