package ru.ifmo.ctddev.zban.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.ToolProvider;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

/**
 * Class implements  {@link info.kgeorgiy.java.advanced.implementor.JarImpler}
 * <p>
 * It generates implementation of interfaces and classes, if they can be implemented/extended.
 * Result classes are non-abstract and compiling. All abstract methods of parent return their default values.
 * The result class name will have <i>Impl</i> suffix.
 * <p>
 * If it is impossible to extend/implement input class/interface, then
 * {@link info.kgeorgiy.java.advanced.implementor.ImplerException} will be thrown
 * <p>
 * @author izban
 * @see info.kgeorgiy.java.advanced.implementor.JarImpler
 */
public class Implementor implements JarImpler {
    /**
     * endLine symbol. May be different on different systems.
     */
    private static final String endLine = System.getProperty("line.separator");

    /**
     * Tab symbol for resulting {@code .java} file.
     */
    private static final String tab = "    ";

    /**
     *  Generates code for class in argument.
     *
     * @param token class to implement.
     * @return string value implemented {@code .java} source.
     * @throws ImplerException if it is impossible to extend class.
     */
    String getImplementation(Class<?> token) throws ImplerException {
        StringBuilder s = new StringBuilder();

        if (token.isPrimitive()) {
            throw new ImplerException(token.getCanonicalName() + " is primitive, can't be implemented");
        }
        if (Modifier.isFinal(token.getModifiers())) {
            throw new ImplerException(token.getCanonicalName() + " is final, can't be implemented");
        }

        String name = token.getSimpleName() + "Impl";
        s.append("@SuppressWarnings({\"unchecked\", \"deprecation\"})").append(endLine);
        s.append("class ").append(name);
        if (token.isInterface()) {
            s.append(" implements ").append(token.getSimpleName());
        } else {
            s.append(" extends ").append(token.getSimpleName());
        }
        s.append(" {").append(endLine);
        s.append(endLine);

        boolean isAllConstructorsPrivate = token.getDeclaredConstructors().length != 0;
        for (Constructor<?> constructor : token.getDeclaredConstructors()) {
            if ((constructor.getModifiers() & Modifier.PRIVATE) != 0) {
                continue;
            }
            isAllConstructorsPrivate = false;
            s.append(tab);
            s.append(Modifier.toString(constructor.getModifiers() & (~Modifier.TRANSIENT) ));
            s.append(" ").append(name).append("(");
            for (int i = 0; i < constructor.getParameterCount(); i++) {
                s.append(constructor.getParameterTypes()[i].getCanonicalName()).append(" a").append(i);
                if (i + 1 < constructor.getParameterCount()) {
                    s.append(", ");
                }
            }
            s.append(")");
            s.append(" throws java.lang.Throwable");
            s.append(" {").append(endLine);
            s.append(tab).append(tab).append("super(");
            for (int i = 0; i < constructor.getParameterCount(); i++) {
                s.append("a").append(i);
                if (i + 1 < constructor.getParameterCount()) {
                    s.append(", ");
                }
            }
            s.append(");");
            s.append(endLine);
            s.append(tab).append("}").append(endLine);
            s.append(endLine);
        }
        if (isAllConstructorsPrivate) {
            throw new ImplerException(token.getName() + " has no constructor");
        }

        for (Method method : getMethods(token)) {
            if ((method.getModifiers() & Modifier.ABSTRACT) == 0) {
                continue;
            }
            s.append(tab);
            s.append(Modifier.toString((~Modifier.ABSTRACT) & (~Modifier.TRANSIENT) & method.getModifiers()));
            s.append(" ").append(method.getReturnType().getCanonicalName());
            s.append(" ").append(method.getName());
            s.append("(");
            for (int i = 0; i < method.getParameterCount(); i++) {
                s.append(method.getParameterTypes()[i].getCanonicalName()).append(" a").append(i);
                if (i + 1 < method.getParameterCount()) {
                    s.append(", ");
                }
            }
            s.append(")");
            if (method.getExceptionTypes().length != 0) {
                s.append(" throws ");
                for (int i = 0; i < method.getExceptionTypes().length; i++) {
                    s.append(method.getExceptionTypes()[i].getCanonicalName());
                    if (i + 1 < method.getExceptionTypes().length) {
                        s.append(", ");
                    }
                }
            }
            s.append(" {").append(endLine);
            s.append(tab).append(tab).append("return ").append(getDefaultValue(method.getReturnType())).append(";");
            s.append(endLine);
            s.append(tab).append("}").append(endLine);
            s.append(endLine);
        }

        s.append("}").append(endLine);
        return s.toString();
    }

    /**
     * dfs for all parents of this class to find all abstract methods
     * .
     * @param cls current class to evaluate
     * @param abstractMethods {@link java.util.HashSet} with abstract methods
     * @param overridedMethods {@link java.util.HashSet} with overrided methods
     */
    private void dfs(Class<?> cls, HashSet<MyMethod> abstractMethods, HashSet<MyMethod> overridedMethods) {
        for (Method method : cls.getDeclaredMethods()) {
            if ((Modifier.ABSTRACT & method.getModifiers()) != 0) {
                abstractMethods.add(new MyMethod(method));
            } else {
                overridedMethods.add(new MyMethod(method));
            }
        }
        if (cls.getSuperclass() != null) {
            dfs(cls.getSuperclass(), abstractMethods, overridedMethods);
        }
        for (Class<?> par : cls.getInterfaces()) {
            dfs(par, abstractMethods, overridedMethods);
        }
    }

    /**
     * returns all abstract methods of class
     *
     * @param cls class which methods you are need
     * @return abstract methods
     */
    Method[] getMethods(Class<?> cls) {
        HashSet<MyMethod> abstractMethods = new HashSet<>();
        HashSet<MyMethod> overridedMethods = new HashSet<>();
        dfs(cls, abstractMethods, overridedMethods);
        ArrayList<Method> result = new ArrayList<>();
        for (MyMethod myMethod : abstractMethods) {
            if (!overridedMethods.contains(myMethod)) {
                result.add(myMethod.toMethod());
            }
        }
        Method res[] = new Method[result.size()];
        for (int i = 0; i < result.size(); i++) {
            res[i] = result.get(i);
        }
        return res;
    }

    /**
     * Return default value for given class.
     *
     * @param cls class for which you are need default value.
     * @return default value of the class.
     */
    String getDefaultValue(Class cls) {
        if (cls.isPrimitive()) {
            if (cls.equals(int.class)) {
                return "0";
            }
            if (cls.equals(long.class)) {
                return "0L";
            }
            if (cls.equals(byte.class)) {
                return "(byte)0";
            }
            if (cls.equals(short.class)) {
                return "(short)0";
            }
            if (cls.equals(void.class)) {
                return "";
            }
            if (cls.equals(float.class)) {
                return "0.0f";
            }
            if (cls.equals(double.class)) {
                return "0.0d";
            }
            if (cls.equals(int.class)) {
                return "(char)0";
            }
            if (cls.equals(boolean.class)) {
                return "false";
            }
            return "0";
        }
        return "null";
    }

    /**
     * Creates implementation for class token in root directory.
     *
     * @param token type token to create implementation for.
     * @param root root directory.
     * @throws ImplerException if can't implement this
     */
    @Override
    public void implement(Class<?> token, File root) throws ImplerException {
        if (token.getPackage() == null) {
            throw new ImplerException(token.getCanonicalName() + " has null package");
        }
        String fileDir = root.getAbsolutePath() + File.separator
                + token.getPackage().getName().replace('.', File.separatorChar) + File.separator;
        if (!new File(fileDir).exists()) {
            if (!new File(fileDir).mkdirs()) {
                throw new ImplerException("can't create directory");
            }
        }
        String fileName = fileDir + token.getSimpleName() + "Impl.java";
        File file = new File(fileName);
        try (PrintWriter out = new PrintWriter(file)) {
            String s = "package " + token.getPackage().getName() + ";";
            s += endLine;
            s += endLine;
            s += getImplementation(token);
            out.println(s);
            out.close();
        } catch (FileNotFoundException e) {
            ImplerException exception = new ImplerException("can't create result file");
            exception.addSuppressed(e);
            throw exception;
        }
    }

    /**
     * Creates jar-file with implementation of token class.
     *
     * @param token type token to create implementation for.
     * @param jarFile target <tt>.jar</tt> file.
     * @throws ImplerException if can't implement this
     */
    @Override
    public void implementJar(Class<?> token, File jarFile) throws ImplerException {
        File tempDirectory;
        try {
            tempDirectory = Files.createTempDirectory("tempImpl").toFile();
            tempDirectory.deleteOnExit();
        } catch (IOException e) {
            throw new ImplerException("can't create temporary directory");
        }
        implement(token, tempDirectory);
        String sourcePath = token.getPackage().getName().replace(".", File.separator) + File.separator;
        ArrayList<String> args = new ArrayList<>();
        args.add(tempDirectory.getAbsolutePath() + File.separator + sourcePath + token.getSimpleName() + "Impl.java");
        args.add("-cp");
        args.add(tempDirectory.getPath() + File.pathSeparator + System.getProperty("java.class.path"));
        int x = ToolProvider.getSystemJavaCompiler().run(null, null, null, args.toArray(new String[args.size()]));

        if (x != 0) {
            throw new ImplerException("can't compile file");
        }
        String compiledClassPath = sourcePath + token.getSimpleName() + "Impl.class";

        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        try (JarOutputStream out = new JarOutputStream(new FileOutputStream(jarFile), manifest);
            FileInputStream in = new FileInputStream(tempDirectory + File.separator + compiledClassPath)) {
            out.putNextEntry(new ZipEntry(compiledClassPath));

            final int BUFFER_SIZE = 2048;
            byte[] buffer = new byte[BUFFER_SIZE];
            int readed;
            while ((readed = in.read(buffer)) > 0) {
                out.write(buffer, 0, readed);
            }
            out.closeEntry();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * produces jar archive with implementation of class.
     * <p>
     * if flag in {@code args[0]} is "-jar", then it writes to file {@code args[2]} implementation of class
     * {@code args[1]}
     * .
     * @param args flag, class to extend and .jar-file
     */
    public static void main(String args[]) {
        if (args == null || args.length != 3 || args[0] == null || !args[0].equals("-jar") || args[1] == null || args[2] == null) {
            System.err.println("incorrect arguments");
            return;
        }
        try {
            new Implementor().implementJar(Class.forName(args[1]), new File(args[2]));
        } catch (ImplerException e) {
            System.err.println("can't build jar");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.err.println("class doesn't exist");
            e.printStackTrace();
        }

    }
}
