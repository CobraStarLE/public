package com.cyser.base.utils;

import com.cyser.base.annotations.TimeFormat;
import com.cyser.base.bean.FieldDefinition;
import com.cyser.base.bean.TypeDefinition;
import com.cyser.base.enums.ClassTypeEnum;
import com.cyser.base.enums.DataTypeEnum;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.ObjectUtils;

import java.beans.Transient;
import java.lang.reflect.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class ClassUtil {

    private ClassUtil() {
    }

    /**
     * 判断一个类是否有范型,例如<br/>
     * <pre>
     * {@code
     * List<String>、List<String>[]
     * }
     * </pre>
     * 都算作有范型
     *
     * @param clazz
     * @return true:是
     */
    public static boolean isGenericClass(Class<?> clazz) {
        if (clazz.isArray()) {
            return isGenericClass(clazz.getComponentType());
        }
        // 获取类的类型参数
        TypeVariable<? extends Class<?>>[] typeParameters = clazz.getTypeParameters();

        // 如果类的类型参数不为空，则认为是泛型类
        return typeParameters.length > 0;
    }

    /**
     * 获取可序列化字段
     *
     * @param clazz
     * @return
     */
    public static List<Field> getSerializableFields(Class clazz) {
        List<Field> fields = Lists.newArrayList(getAllFieldsMap(clazz).values()); // 目标类所有字段
        return getSerializableFields(fields);
    }

    /**
     * 获取可序列化字段
     *
     * @param fields
     * @return
     */
    public static List<Field> getSerializableFields(Collection<Field> fields) {
        List<Field> list = new ArrayList<>();
        for (Field field : fields) {
            if (isSerializableField(field)) list.add(field);
        }
        return list;
    }

    /**
     * 判断类是否可序列化
     *
     * @param clazz
     * @return true:可序列化 <br>
     * false:不可序列化
     */
    public static boolean isSerializableClass(Class clazz) {
        boolean t =
                Modifier.isTransient(clazz.getModifiers())
                        || Modifier.isStatic(clazz.getModifiers())
                        || Modifier.isFinal(clazz.getModifiers());

        return !t;
    }

    /**
     * 判断字段是否可序列化
     *
     * @param field
     * @return true:可序列化 <br>
     * false:不可序列化
     */
    public static boolean isSerializableField(Field field) {
        boolean t1 = field.getAnnotation(Transient.class) != null;
        boolean t2 =
                Modifier.isTransient(field.getModifiers())
                        || Modifier.isStatic(field.getModifiers())
                        || Modifier.isFinal(field.getModifiers());

        return !t1 && !t2;
    }


    /**
     * 获取一个类所有的声明字段(包含超类)
     *
     * <p><b>Note:</b>如果类中包含与超类相同名称的字段，则返回该类中字段
     *
     * @param clazz
     * @return
     */
    public static Collection<Field> getAllFieldsCollection(Class clazz) {
        Map<String, Field> map = new HashMap<>();
        Class<?> currentClass = clazz;
        while (currentClass != null) {
            final Field[] declaredFields = currentClass.getDeclaredFields();
            for (Field field : declaredFields) {
                if (!map.containsKey(field.getName())) map.put(field.getName(), field);
            }
            currentClass = currentClass.getSuperclass();
        }
        return map.values();
    }

    /**
     * 获取一个类所有的声明字段(包含超类)
     *
     * <p><b>Note:</b>如果类中包含与超类相同名称的字段，则返回该类中字段
     *
     * @param clazz
     * @return
     */
    public static Map<String, Field> getAllFieldsMap(Class clazz) {
        Map<String, Field> map = new HashMap<>();
        Class<?> currentClass = clazz;
        while (currentClass != null) {
            final Field[] declaredFields = currentClass.getDeclaredFields();
            for (Field field : declaredFields) {
                if (!map.containsKey(field.getName())) map.put(field.getName(), field);
            }
            currentClass = currentClass.getSuperclass();
        }
        return map;
    }

    /**
     * 检查一个Class是否可以分配给另一个Class的变量
     *
     * @param cls
     * @param toClass
     * @param autoboxing
     * @return true:可分配 <br>
     * false:不可分配
     */
    public static boolean isAssignable(
            final Class<?> cls, final Class<?> toClass, final boolean autoboxing) {
        if (toClass == null) {
            return false;
        }
        if (cls == null) {
            return !toClass.isPrimitive();
        }
        Class<?> _cls = cls;
        Class<?> _toClass = toClass;
        if (_cls.isPrimitive()) {
            _cls = ClassUtils.primitiveToWrapper(_cls);
        }
        if (_toClass.isPrimitive()) {
            _toClass = ClassUtils.primitiveToWrapper(_toClass);
        }
        if (_cls.equals(_toClass)) {
            return true;
        }
        if (_cls.isAssignableFrom(String.class)
                && ClassUtils.isPrimitiveOrWrapper(_toClass)
                && (!(Void.TYPE.equals(_toClass) || Boolean.TYPE.equals(_toClass)))) {
            return true;
        }
        if (_toClass.isAssignableFrom(String.class)
                && ClassUtils.isPrimitiveOrWrapper(_cls)
                && (!(Void.TYPE.equals(_cls) || Boolean.TYPE.equals(_cls)))) {
            return true;
        }
        if (isCollectionOrMapOrArray(_cls) && isCollectionOrMapOrArray(_toClass)) {
            //在这里并不是真正的判断可以分配变量，真正可以分配变量需要判断范型类型是否可以
            return true;
        }

        return ClassUtils.isAssignable(_cls, _toClass, autoboxing);
    }

    /**
     * 判断Class是否是集合或者Map或者数组
     *
     * @return
     */
    public static boolean isCollectionOrMapOrArray(Class clazz) {
        return isCollection(clazz) || isMap(clazz) || isArray(clazz);
    }

    /**
     * 判断Class是否是集合
     *
     * @return
     */
    public static boolean isCollection(Class clazz) {
        return Collection.class.isAssignableFrom(clazz);
    }

    /**
     * 判断Class是否是Map
     *
     * @return
     */
    public static boolean isMap(Class clazz) {

        return Map.class.isAssignableFrom(clazz);
    }

    /**
     * 判断Class是否是数组
     *
     * @return
     */
    public static boolean isArray(Class clazz) {
        return clazz.isArray();
    }

    public static TypeDefinition parseType(Type type) throws ClassNotFoundException {
        TypeDefinition td = new TypeDefinition();
        td.raw_type = type;
        td.class_type = ClassTypeEnum.valueOf(type);
        td.raw_Type_class = type.getClass();
        td.runtime_class=td.raw_Type_class;
        if (td.class_type == ClassTypeEnum.Unknown) {
            throw new RuntimeException("未识别的类型: " + ((type == null) ? "[null]" : type.toString()));
        }
        if (td.class_type == ClassTypeEnum.ParameterizedType) {
            td.isGeneric = true;
            ParameterizedType parameter_type = (ParameterizedType) type;
            td.runtime_class = Class.forName(parameter_type.getRawType().getTypeName());
            Type[] actualTypeArguments = parameter_type.getActualTypeArguments();
            TypeDefinition[] parameter_type_Defines = new TypeDefinition[actualTypeArguments.length];
            TypeVariable[] typeVariables= td.runtime_class.getTypeParameters();
            Map<String,Class> parameter_type_corresponds=new HashMap<>();
            for (int i = 0; i < actualTypeArguments.length; i++) {
                parameter_type_Defines[i] = parseType(actualTypeArguments[i]);
                parameter_type_corresponds.put(typeVariables[i].getName(),parameter_type_Defines[i].runtime_class);
            }
            td.parameter_type_corresponds=parameter_type_corresponds;
            td.parameter_type_Defines = parameter_type_Defines;
        } else if (td.class_type == ClassTypeEnum.Class) {
            Class clazz = (Class) type;
            td.isGeneric = isGenericClass(clazz);

            //判断是否是数组
            if (clazz.getComponentType() != null) {
                td.isArray = true;
                TypeDefinition componetClassDefine = parseType(clazz.getComponentType());
                td.componetClassDefine = componetClassDefine;
            } else {
                td.runtime_class = Class.forName(td.raw_type.getTypeName());
                td.isSerializable = isSerializableClass(td.runtime_class);
                // 判断是否是基本类型
                td.isPrimitive = td.runtime_class.isPrimitive();
                // 判断是否是基本封装类型
                td.isPrimitiveWrapper = ClassUtils.isPrimitiveWrapper(td.runtime_class);
                // 判断是否是基本类型或者基本封装类型
                td.isPrimitiveOrWrapper = td.isPrimitive || td.isPrimitiveWrapper;
                Map<String,Class> parameter_type_corresponds=new HashMap<>();
                if(td.isGeneric){
                    TypeVariable<? extends Class<?>>[] typeVariables = clazz.getTypeParameters();
                    for (int i = 0; i < typeVariables.length; i++) {
                        parameter_type_corresponds.put(typeVariables[i].getName(),Object.class);
                    }
                }
                td.parameter_type_corresponds=parameter_type_corresponds;

            }
        } else if (td.class_type == ClassTypeEnum.GenericArrayType) {
            td.isGeneric = true;
            td.isArray = true;
            GenericArrayType genricArrayType = (GenericArrayType) type;
            Type genericComponentType = genricArrayType.getGenericComponentType();
            td.genericComponentType = parseType(genericComponentType);
        } else if (td.class_type == ClassTypeEnum.WildcardType) {
            td.isGeneric = true;
            WildcardType wildcardType = (WildcardType) type;
            Type[] upperBounds = wildcardType.getUpperBounds();
            if (ObjectUtils.isNotEmpty(upperBounds)) {
                td.upperBounds = parseType((Class) upperBounds[0]);
            }
            Type[] lowerBounds = wildcardType.getLowerBounds();
            if (ObjectUtils.isNotEmpty(lowerBounds)) {
                td.lowerBounds = parseType((Class) lowerBounds[0]);
            }
        } else if (td.class_type == ClassTypeEnum.TypeVariable) {
            td.isGeneric = false;
        }
        td.data_type=DataTypeEnum.valueOf(td.runtime_class);
        return td;
    }

    /**
     * 解析字段
     *
     * @param field
     * @return
     */
    public static FieldDefinition parseField(Field field,Map<String,Class> parameter_type_corresponds) throws ClassNotFoundException {
        Type type = field.getType();
        Type generic_type = field.getGenericType();
        FieldDefinition fd = new FieldDefinition();
        if (!field.isAccessible()) field.setAccessible(true);
        fd.field = field;
        fd.raw_type = type;
        fd.genericType = generic_type;
        fd.raw_Type_class = ClassUtils.getClass(type.getTypeName());
        fd.runtime_class=fd.raw_Type_class;
        // 如果是泛型
        if (generic_type instanceof ParameterizedType) {
            fd.isGeneric = true;
            Type[] param_types = ((ParameterizedType) generic_type).getActualTypeArguments();
            Class[] parameter_Type_classes = new Class[param_types.length];
            for (int i = 0; i < param_types.length; i++) {
                ClassTypeEnum classType=ClassTypeEnum.valueOf(param_types[i]);
                if (classType==ClassTypeEnum.TypeVariable) {//如果是List<V>这种
                    parameter_Type_classes[i] = parameter_type_corresponds.get(param_types[i].getTypeName());
                }else if(classType==ClassTypeEnum.Class){//如果是List<Cat>这种
                    parameter_Type_classes[i] = ClassUtils.getClass(param_types[i].getTypeName());
                }
            }
            fd.parameter_Type_classes = parameter_Type_classes;
        }else if(fd.raw_type instanceof TypeVariable){//如果是V v这种
            TypeVariable _type= (TypeVariable) fd.raw_type;
            fd.runtime_class=parameter_type_corresponds.get(_type.getTypeName());
        }else if(fd.raw_type instanceof WildcardType){//如果是List<?>这种

        }
        fd.data_type= DataTypeEnum.valueOf(fd.runtime_class);
        // 判断是否是基本类型
        fd.isPrimitive = fd.raw_Type_class.isPrimitive();
        // 判断是否是基本封装类型
        fd.isPrimitiveWrapper = ClassUtils.isPrimitiveWrapper(fd.raw_Type_class);
        // 判断是否是基本类型或者基本封装类型
        fd.isPrimitiveOrWrapper = fd.isPrimitive || fd.isPrimitiveWrapper;
        // 判断是否是日期字段
        if (ClassUtils.isAssignable(fd.raw_Type_class, LocalDate.class)
                || ClassUtils.isAssignable(fd.raw_Type_class, LocalDateTime.class)
                || ClassUtils.isAssignable(fd.raw_Type_class, Date.class)
                || (ClassUtils.isAssignable(fd.raw_Type_class, String.class)
                && field.getAnnotation(TimeFormat.class) != null)) {
            fd.isTime = true;
            TimeFormat timeFormat = field.getAnnotation(TimeFormat.class);
            if (timeFormat != null) fd.timeFormat = timeFormat.value();
        }
        // 判断是否是可序列化字段
        if (!isSerializableField(field)) fd.isSerializable = false;
        return fd;
    }
}
