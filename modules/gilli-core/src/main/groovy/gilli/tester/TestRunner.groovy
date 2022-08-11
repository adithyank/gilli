package gilli.tester

class TestNgRunner
{
    public void runTests(List<String> classMatchingPatterns)
    {

    }

    static void main(String[] args) {
        args.each {println "test runner : class : $it"}
    }
}
