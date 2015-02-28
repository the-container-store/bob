package com.twoqubed.bob.processor;

import com.twoqubed.bob.annotation.Built;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import static javax.lang.model.element.ElementKind.*;
import static javax.tools.Diagnostic.Kind.*;

class ElementProcessor {

    private final Messager messager;

    ElementProcessor(Messager messager) {
        this.messager = messager;
    }

    BuilderMetadata handleAnnotatedClass(Element e) throws BuilderException {
        Built built = e.getAnnotation(Built.class);
        BuilderMetadata metadata = new BuilderMetadata(built.generateCopyMethod());
        TypeElement classElement = (TypeElement) e;
        PackageElement packageElement = (PackageElement) classElement.getEnclosingElement();

        messager.printMessage(NOTE, "annotated class: " + classElement.getQualifiedName(), e);

        metadata.fqClassName = classElement.getQualifiedName().toString();
        metadata.className = classElement.getSimpleName().toString();
        metadata.packageName = packageElement.getQualifiedName().toString();

        addConstructorParameters(e, metadata);
        return metadata;
    }

    private void addConstructorParameters(Element e, BuilderMetadata metadata) throws BuilderException {
        ExecutableElement constructorElement = findConstructor(e);
        handleAnnotatedConstructor(metadata, constructorElement);
    }

    private ExecutableElement findConstructor(Element e) throws BuilderException {
        ExecutableElement match = null;
        for (Element enclosed : e.getEnclosedElements()) {
            if (enclosed.getKind() == CONSTRUCTOR) {
                ExecutableElement constructorElement = (ExecutableElement) enclosed;
                if (constructorElement.getParameters().size() > 0) {
                    if (match != null) {
                        throw new BuilderException("Ambiguous constructors");
                    }
                    match = constructorElement;
                }
            }
        }
        return match;
    }

    private void handleAnnotatedConstructor(BuilderMetadata builderMetadata, ExecutableElement constructorElement) {
        for (VariableElement each : constructorElement.getParameters()) {
            String simpleName = each.getSimpleName().toString();

            TypeMirror typeMirror = each.asType();
            String type = typeMirror.toString();

            ConstructorParam constructorParam = new ConstructorParam(simpleName, type);

            builderMetadata.addConstructorParam(constructorParam);
        }
    }
}
