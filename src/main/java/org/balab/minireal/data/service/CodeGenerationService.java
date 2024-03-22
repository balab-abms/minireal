package org.balab.minireal.data.service;

import com.squareup.javapoet.*;
import lombok.Getter;
import lombok.Setter;
import org.balab.minireal.data.entity.SimForm;
import org.simreal.annotation.*;
import org.springframework.stereotype.Service;
import sim.engine.SimState;
import sim.engine.Steppable;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import com.squareup.javapoet.JavaFile;

@Service
public class CodeGenerationService {
    private final String model_pkg_name = "com.example.abm";
    // Model generating method
    public JavaFile generateBaseModel(SimForm sim_data){
        String model_name = sim_data.getModel_name();
        String agent_name = sim_data.getAgent_name();
        // generate field
        FieldSpec serial_id_field = FieldSpec.builder(long.class, "serialVersionUID")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC)
                .initializer("1L")
                .build();

        FieldSpec popln_field = FieldSpec.builder(int.class, "population")
                .addModifiers(Modifier.PRIVATE)
                .build();

        FieldSpec envt_field = FieldSpec.builder(sim_data.getField_type(), sim_data.getField_name())
                .addModifiers(Modifier.PUBLIC)
                .build();

        // generate methods
        MethodSpec create_agents_method = MethodSpec.methodBuilder("createAgents")
                .addModifiers(Modifier.PRIVATE)
//                .addParameter(int.class, "popln")
                .returns(void.class)
                .addComment("todo: clear model field")
                .addComment("generate agents and add to model")
                .beginControlFlow("for($T i=0; i<$N; i++)", int.class, popln_field)
                .addStatement("$L temp_agt = new $L(this)", agent_name, agent_name)
                .addStatement("schedule.scheduleRepeating(temp_agt)")
                .addComment("todo: add agent to model field")
                .endControlFlow()
                .build();

        MethodSpec draw_chart_method = MethodSpec.methodBuilder("drawChart")
                .addModifiers(Modifier.PUBLIC)
                .returns(int.class)
                .addAnnotation(AnnotationSpec.builder(SimChart.class)
                        .addMember("name", "\"line_chart\"")
                        .build())
                .addComment("insert chart generation code here")
                .addStatement("return 0")
                .build();

        // define list of agent class (object) type for db persisting method
        ClassName agent_class = ClassName.get(model_pkg_name, agent_name);
        ClassName arrayList_class = ClassName.get("java.util", "ArrayList");
        TypeName listof_agents_class = ParameterizedTypeName.get(arrayList_class, agent_class);

        MethodSpec db_visual_method = MethodSpec.methodBuilder("persistVisualizeAgents")
                .addModifiers(Modifier.PUBLIC)
                .returns(listof_agents_class)
                .addAnnotation(AnnotationSpec.builder(SimDB.class)
                        .addMember("name", "\"$L_db\"", agent_name)
                        .build())
                .addComment("return list of agents whose data will be persisted and visualized")
                .addStatement("return null")
                .build();

        // generate constructor
        ParameterSpec popln_param = ParameterSpec.builder(int.class, "population")
                .addAnnotation(AnnotationSpec.builder(SimParam.class)
                        .addMember("value", "\"$L\"", sim_data.getPopln().longValue())
                        .build())
                .build();

        MethodSpec model_constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(popln_param)
                .addStatement("super(System.currentTimeMillis())")
                .addComment("initialize model environment field")
                .addStatement("this.$L = new $T()", sim_data.getField_name(), sim_data.getField_type())
                .addStatement("this.population = population")
                .addComment("generate agents")
                .addStatement("$N()", create_agents_method)
                .build();

        // define main method
        String model_obj_name = "model_obj";
        String is_setp_var_name = "is_step";
        MethodSpec main_method = MethodSpec.methodBuilder("main")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(void.class)
                .addParameter(String[].class, "args")
                .addStatement("$L $L = new $L($L)", model_name, model_obj_name, model_name, sim_data.getPopln().longValue())
                .beginControlFlow("do")
                .addStatement("$T $L = $L.schedule.step($L)", boolean.class, is_setp_var_name, model_obj_name, model_obj_name)
                .beginControlFlow("if(!$L)", is_setp_var_name)
                .addStatement("break")
                .endControlFlow()
                .addStatement("System.out.println(\"tick = \" + $L.schedule.getSteps())", model_obj_name)
                .endControlFlow()
                .addStatement("while($L.schedule.getSteps() < 100)", model_obj_name)
                .build();

        // put it all together in the model class
        TypeSpec model_class = TypeSpec.classBuilder(model_name)
                .addModifiers(Modifier.PUBLIC)
                .superclass(SimState.class)
                .addAnnotation(SimModel.class)
                .addAnnotation(Getter.class)
                .addAnnotation(Setter.class)
                .addField(serial_id_field)
                .addField(popln_field)
                .addField(envt_field)
                .addMethod(model_constructor)
                .addMethod(create_agents_method)
                .addMethod(draw_chart_method)
                .addMethod(db_visual_method)
                .addMethod(main_method)
                .build();

        JavaFile model_file = JavaFile.builder(model_pkg_name, model_class).build();
        return model_file;
    }

    // Agent generating method
    public JavaFile generateBaseAgent(SimForm sim_data){
        // define variable names here
        String model_var_name = "model";

        // generate field
        FieldSpec serial_id_field = FieldSpec.builder(long.class, "serialVersionUID")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC)
                .initializer("1L")
                .build();

        // define model class path & field
        ClassName model_class = ClassName.get(model_pkg_name, sim_data.getModel_name());
        FieldSpec model_field = FieldSpec.builder(model_class, model_var_name)
                .addModifiers(Modifier.PUBLIC)
                .build();

        FieldSpec id_field = FieldSpec.builder(String.class, "agent_id")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(SimField.class)
                .build();

        FieldSpec step_field = FieldSpec.builder(Long.class, "step")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(SimField.class)
                .build();

        // define constructor
        String id_stmt = "this.agent_id = String.valueOf($L.random.nextInt($L.getPopulation()))";
        MethodSpec agent_constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(model_class, model_var_name)
                .addStatement("this.$L = $L", model_var_name, model_var_name)
                .addStatement(id_stmt, model_var_name, model_var_name)
                .addStatement("this.$N = $L.schedule.getSteps()", step_field, model_var_name)
                .build();

        MethodSpec step_method = MethodSpec.methodBuilder("step")
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addParameter(SimState.class, "sim_state")
                .addComment("update step field")
                .addStatement("this.$N = $L.schedule.getSteps()", step_field, model_var_name)
                .addComment("perform agent actions here")
                .build();

        // put it all together in the agent class
        TypeSpec agent_class = TypeSpec.classBuilder(sim_data.getAgent_name())
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(Steppable.class)
                .addAnnotation(SimAgent.class)
                .addAnnotation(Getter.class)
                .addAnnotation(Setter.class)
                .addField(serial_id_field)
                .addField(model_field)
                .addField(id_field)
                .addField(step_field)
                .addMethod(agent_constructor)
                .addMethod(step_method)
                .build();

        JavaFile agent_file = JavaFile.builder(model_pkg_name, agent_class).build();
        return agent_file;
    }

    // ** a service to create source files from codes
    public File createSourceFile(File base_dir, String filename, String source_code)
    {
        File source_file = null;
        try
        {
            // initialize file object and write to a file in designated path and name
            source_file = new File(base_dir, filename);
            source_file.getParentFile().mkdirs();
            new FileWriter(source_file).append(source_code).close();
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        return source_file;
    }
}
