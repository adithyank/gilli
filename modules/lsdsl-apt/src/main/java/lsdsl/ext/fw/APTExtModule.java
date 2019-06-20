package lsdsl.ext.fw;

import javax.annotation.processing.*;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.File;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.util.*;

public class APTExtModule extends AbstractProcessor
{
    private static final String FILENAME = "META-INF/groovy/org.codehaus.groovy.runtime.ExtensionModule";

    private static final String KEY_MODULE_NAME              = "moduleName";
    private static final String KEY_MODULE_VERSION           = "moduleVersion";
    private static final String KEY_EXTENSION_CLASSES        = "extensionClasses";
    private static final String KEY_STATIC_EXTENSION_CLASSES = "staticExtensionClasses";

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv)
    {
        super.init(processingEnv);
    }

    private Set<String> getAnnotatedClasses(Class<? extends Annotation> a, RoundEnvironment roundEnv)
    {
        Set<String> classNames = new HashSet<>();

        for (Element e : roundEnv.getElementsAnnotatedWith(a))
        {
            if (e.getKind() != ElementKind.CLASS)
            {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "@" + a.getSimpleName() + " annotation can be used only with Class", e);
                return null;
            }

            TypeElement c = (TypeElement) e;
            classNames.add(String.valueOf(c.getQualifiedName()));
        }

        return classNames;
    }

    /**
     * Done this way to take care of incremental compilation also
     *
     * @param p
     * @param key
     * @param newClasses
     */
    private void prepareKey(Properties p, String key, Set<String> newClasses)
    {
        Set<String> finalClasses = new HashSet<>();

        if (p.containsKey(key))
            Arrays.stream(p.getProperty(key).split(",")).filter(c -> !c.trim().isEmpty()).map(String::trim).forEach(finalClasses::add);

        finalClasses.addAll(newClasses);

        p.setProperty(key, String.join(",", finalClasses));

        //System.out.println("val = " + p.getProperty(key));
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
    {
        if (roundEnv.processingOver())
            return true;

//        System.out.println("DDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD");
//        System.out.println("annotations = " + annotations);
//        System.out.println("env = " + roundEnv);
//        System.out.println("over = " + roundEnv.processingOver());
//        System.out.println("error = " + roundEnv.errorRaised());
        Set<String> annotatedExtClasses = getAnnotatedClasses(ExtModule.class, roundEnv);

//        System.out.println("ext = " + annotatedExtClasses);

        if (annotatedExtClasses == null) //error
            return false;

        Set<String> annotatedStaticExtClasses = getAnnotatedClasses(StaticExtModule.class, roundEnv);

//        System.out.println("static ext = " + annotatedStaticExtClasses);

        if (annotatedStaticExtClasses == null) //error
            return false;

        try
        {
            FileObject resource = processingEnv.getFiler().getResource(StandardLocation.CLASS_OUTPUT, "", FILENAME);

            File file = new File(resource.toUri());

            Properties p = new Properties();

            if (file.exists())
                p.load(resource.openReader(true));

            prepareKey(p, KEY_EXTENSION_CLASSES, annotatedExtClasses);
            prepareKey(p, KEY_STATIC_EXTENSION_CLASSES, annotatedStaticExtClasses);

            p.setProperty(KEY_MODULE_NAME, "lsdsl");
            p.setProperty(KEY_MODULE_VERSION, "some-value");

            StringBuilder content = new StringBuilder();

            String[] keys = {KEY_MODULE_NAME, KEY_MODULE_VERSION, KEY_EXTENSION_CLASSES, KEY_STATIC_EXTENSION_CLASSES};

            for (String k : keys)
                content.append(k).append("=").append(p.get(k)).append("\n");

            FileObject w = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", FILENAME, (Element[]) null);
            PrintWriter pw = new PrintWriter(w.openOutputStream());
            pw.println(content);
            pw.close();

        }
        catch (Exception ex)
        {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, ex.toString());
            ex.printStackTrace();
            return false;
        }


        return true;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes()
    {
        return new HashSet<>(Arrays.asList(ExtModule.class.getName(), StaticExtModule.class.getName()));
    }
}
