package io.simpledi.context;

import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * @author Hamza Ouni
 */
public class SimpleDITest {

    private SimpleDI simpleDI;

    /**
     * For better readability we use inner classes in all test methods.
     * the drawback of this approach is that you have to provide the outer instance
     * (this instance of {@link io.simpledi.context.SimpleDITest}) as first param to
     * {@link java.lang.reflect.Constructor#newInstance(Object...)}.
     *
     * To simulate this we override the method {@link io.simpledi.context.SimpleDI#newInstance(java.lang.reflect.Constructor, Object...)}
     * and modify the given args-array so that the first argument is always <code>this</code>.
     *
     * @throws Exception
     */
    @Before
    public void setup() throws Exception{

        simpleDI = new SimpleDI(){
            @Override
            Object newInstance(Constructor<?> constructor, Object... originalArgs) throws IllegalAccessException, InvocationTargetException, InstantiationException {
                // maybe this isn't the fastest/coolest way of inserting 'this' as the first element of the args array
                // but it works :-)
                List<Object> tmp = new ArrayList<>();
                tmp.add(SimpleDITest.this);
                tmp.addAll(Arrays.asList(originalArgs));
                return super.newInstance(constructor,tmp.toArray());
            }
        };
    }

    @Test
    public void success_publicNoArgConstructor(){
        class Example {
            public Example(){
            }
        }

        final Example instance = simpleDI.getInstance(Example.class);
        assertThat(instance).isNotNull();
    }

    @Test(expected = IllegalStateException.class)
    public void fail_multipleConstructors_NotAnnotated(){
        class Example{
            public Example(){
            }

            public Example(String helloworld){
            }
        }

        simpleDI.getInstance(Example.class);
    }

    @Test(expected = IllegalStateException.class)
    public void fail_multipleConstructors_TwoWithAnnotation(){
        class Example{
            @Inject
            public Example(){
            }

            @Inject
            public Example(String helloworld){
            }
        }

        simpleDI.getInstance(Example.class);
    }


    @Test
    public void success_multipleConstructors_OneWithAnnotation(){
        class Example {
            @Inject
            public Example(){
            }

            public Example(String helloWorld){
            }
        }

        final Example instance = simpleDI.getInstance(Example.class);
        assertThat(instance).isNotNull();
    }

    @Test
    public void success_multipleConstructors_OnlyOnePublic(){
        class Example {
            public Example(){
            }

            Example(String helloWorld){
            }
        }

        final Example instance = simpleDI.getInstance(Example.class);
        assertThat(instance).isNotNull();
    }



    @Test(expected = IllegalStateException.class)
    public void fail_noDeclaredConstructor(){
        class Example {
            private Example(){
            }
        }

        simpleDI.getInstance(Example.class);
    }
}
