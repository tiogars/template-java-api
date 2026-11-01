help:
	@echo "Available targets:"
	@echo "  help       Show this help message"
	@echo "  build      Build the project"
	@echo "  run        Run the project"
	@echo "  test       Run tests"
	@echo "  checkstyle Run checkstyle"
	@echo "  clean      Clean the project"

test:
	mvn -f pom.xml test

checkstyle:
	mvn -f pom.xml checkstyle:check

build:
	mvn -f pom.xml package

run:
	mvn -f pom.xml -pl api spring-boot:run

clean:
	mvn -f pom.xml clean