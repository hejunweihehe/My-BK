package com.fanbianyi.mybutterknightcompiler;

import com.fanbianyi.mybutterknife.BindView;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

@AutoService(Processor.class)
public class MyButterKnifeProcessor extends AbstractProcessor {
    private Filer filer;
    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        filer = processingEnvironment.getFiler();
        messager = processingEnvironment.getMessager();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        for (Class<? extends Annotation> annotation : getSupportedAnnotations()) {
            types.add(annotation.getCanonicalName());
        }
        return types;
    }

    private Set<Class<? extends Annotation>> getSupportedAnnotations() {
        Set<Class<? extends Annotation>> annotations = new LinkedHashSet<>();
        annotations.add(BindView.class);
        return annotations;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        String genaratePackageName = "";
        Map<String, List<Element>> buildingMap = new HashMap<>();
        //set中包含了所有getSupportedAnnotations方法中返回的注解
        for (TypeElement te : set) {
            //getElementsAnnotatedWith获取使用了注解的元素
            for (Element e : roundEnvironment.getElementsAnnotatedWith(te)) {
                // 准备在gradle的控制台打印信息
                Messager messager = processingEnv.getMessager();
                List<Element> list = new ArrayList<>();
                // 打印
                messager.printMessage(Diagnostic.Kind.NOTE, "Printing: " + e.toString());
                messager.printMessage(Diagnostic.Kind.NOTE, "Printing: " + e.getSimpleName());
                messager.printMessage(Diagnostic.Kind.NOTE, "Printing: " + e.getEnclosingElement().toString());
                //过滤private的控件
                boolean isPrivate = false;
                for (Modifier modifier : e.getModifiers()) {
                    if (modifier == Modifier.PRIVATE) {
                        isPrivate = true;
                        break;
                    }
                }
                if (isPrivate) {
                    continue;
                }
                //按照使用注解的元素的类名，将Element分好
                if (null != buildingMap.get(e.getEnclosingElement().toString())) {
                    list = buildingMap.get(e.getEnclosingElement().toString());
                } else {
                    list = new ArrayList<>();
                }
                list.add(e);
                buildingMap.put(e.getEnclosingElement().toString(), list);

                // 获取父元素的全类名, 用来生成包名
                String enclosingQualifiedName;
                if (e.getEnclosingElement() instanceof PackageElement) {
                    enclosingQualifiedName = ((PackageElement) e.getEnclosingElement()).getQualifiedName().toString();
                } else {
                    enclosingQualifiedName = ((TypeElement) e.getEnclosingElement()).getQualifiedName().toString();
                }
                // 生成的类名
                genaratePackageName = enclosingQualifiedName.substring(0, enclosingQualifiedName.lastIndexOf('.'));
            }
        }

        for (String name : buildingMap.keySet()) {
            List<Element> list = buildingMap.get(name);
            // 创建Java文件。
            // createSourceFile传递的参数是包含包名的完整类名。
            // 如果文件名字中有包名，那么会自动创建好包目录，并且以后最后一个小数点后面的字符作为类名
            try {

                JavaFileObject f = processingEnv.getFiler().createSourceFile(name + "_Binding");
                // 在控制台输出文件路径
                messager.printMessage(Diagnostic.Kind.NOTE, "Printing: " + f.toUri());
                Writer w = f.openWriter();
                try {
                    PrintWriter pw = new PrintWriter(w);
                    //要注意，这里手动写入的包名，系统是不会自动帮忙构建包目录的。
                    pw.println("package " + genaratePackageName + ";");

                    //生成import语句
                    for (Element e : list) {
                        pw.println("\nimport " + e.asType() + ";");
                    }
                    pw.println("\nimport android.util.Log;");

                    //获取类名简写
                    pw.println("\npublic class " + list.get(0).getEnclosingElement().getSimpleName() + "_Binding" + " { ");
                    pw.println("    public static void " + "initView" + "(" + list.get(0).getEnclosingElement().getSimpleName() + " activity) {");
                    //生成方法中的代码
                    for (Element e : list) {
                        // 包裹注解元素的元素, 也就是其父元素, 比如注解了成员变量或者成员函数, 其上层就是该类
                        Element enclosingElement = e.getEnclosingElement();
                        // 获取注解
                        //TODO 这里额外做一个注解不等于BingView的判断
                        BindView annotation = e.getAnnotation(BindView.class);
                        pw.println("        //" + enclosingElement.toString());
                        pw.println("        //" + enclosingElement.getSimpleName());
                        pw.println("        //\"e: " + e.toString() + " ,e.hashcode: " + e.hashCode() + " ,e.modifier:" + Arrays.toString(e.getModifiers().toArray()) + " ,:" + e.asType() + "\");");
                        pw.println("        Log.d(\"hjw_test\"," + " \"e." + e.toString() + " = \" + activity." + e.toString() + ");");
                        pw.println("        activity." + e.toString() + " = " + "activity.findViewById(" + annotation.value() + ");");
                        pw.println("        Log.d(\"hjw_test\"," + " \"e." + e.toString() + " = \" + activity." + e.toString() + ");");
                    }
                    pw.println("    }");
                    pw.println("}");
                    pw.flush();
                } finally {
                    w.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return false;
    }
}
