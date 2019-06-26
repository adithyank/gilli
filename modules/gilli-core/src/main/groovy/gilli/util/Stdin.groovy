package gilli.util

import gilli.ext.ListInstanceMethodExt
import groovy.json.JsonSlurper

class Stdin
{
    static List<String> lines()
    {
        return System.in.readLines()
    }

    static List<Number> numbers()
    {
        return ListInstanceMethodExt.numbers(lines())
    }

    static Object execLikeAwk(Closure closure)
    {
        ComplexClosureInfo cc = new ComplexClosureInfo()

        closure.delegate = cc
        closure.resolveStrategy = Closure.DELEGATE_FIRST

        closure()
        cc.exec()
    }

    static <T> List<T> asList(Closure<T> closure)
    {
        lines().collect {closure(it)}
    }

    static Object json()
    {
        new JsonSlurper().parse(System.in)
    }

    static Object xml()
    {
        new XmlSlurper().parse(System.in)
    }
}

class ComplexClosureInfo
{
    private Closure eachLineClosure;
    private Closure endClosure;

    void EACHLINE(Closure closure)
    {
        eachLineClosure = closure
    }

    void END(Closure closure)
    {
        endClosure = closure
    }

    Object exec()
    {
        MapClosureDelegate del = new MapClosureDelegate()

        eachLineClosure?.delegate = del

        System.in.eachLine {

            del.line = it

            eachLineClosure?.call()
        }

        return endClosure?.call()
    }
}

