package org.jboss.jsr299.tck.tests.context.passivating.broken.interceptorWithNonPassivatingBeanConstructorParameter;

import java.io.Serializable;

import javax.enterprise.context.SessionScoped;

@SessionScoped @BakedBinding
class BakedBean implements Serializable
{
   void bake() {}
}
