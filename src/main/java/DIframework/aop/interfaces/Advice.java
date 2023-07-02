package DIframework.aop.interfaces;

public interface Advice extends Comparable<Advice> {
    int getPriority();

    int type();

    int compareTo(Advice other);

}
