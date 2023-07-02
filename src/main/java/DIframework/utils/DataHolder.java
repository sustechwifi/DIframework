package DIframework.utils;

import java.util.HashMap;
import java.util.Map;

public class DataHolder<T> {
    private static final Map<String, DataHolder<?>> locals = new HashMap<>();
    private final String id;
    private final ThreadLocal<T> data = new ThreadLocal<>();

    public static <T> T getDataHolder(String id,Class<T> type){
        if (locals.containsKey(id)){
            var holder = locals.get(id);
            return holder.getData(type);
        }else {
            throw new IllegalArgumentException(String.format("找不到 DataHolder id = %s",id));
        }
    }

    public static Object getDataHolder(String id){
        if (locals.containsKey(id)){
            var holder = locals.get(id);
            return holder.getData();
        }else {
            throw new IllegalArgumentException(String.format("找不到 DataHolder id = %s",id));
        }
    }

    public DataHolder(String id) {
        this.id = id;
        locals.put(id, this);
    }

    public DataHolder(String id, T value) {
        this.id = id;
        setData(value);
        locals.put(id, this);
    }

    public void setData(T value) {
        if (this.data.get() == null) {
            this.data.set(value);
        } else {
            Log.error(String.format("当前 ThreadLocal ：%s 已经被赋值过!", id));
        }
    }

    public void clearData() {
        if (this.getData() == null) {
            this.data.remove();
        }
    }

    public void deleteData(){
        clearData();
        locals.remove(this.id);
    }


    public String getId() {
        return id;
    }

    public T getData() {
        return data.get();
    }

    public <O> O getData(Class<O> clazz) {
        return (O) data.get();
    }

}
