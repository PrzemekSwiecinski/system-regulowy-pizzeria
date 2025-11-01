package org.system_regulowy;

import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

public class DroolsEngine {
    private static final KieContainer kieContainer;

    static {
        KieServices ks = KieServices.Factory.get();
        kieContainer = ks.getKieClasspathContainer();
    }

    public static void applyRules(Order order) {
        if (kieContainer == null) {
            System.err.println("Błąd krytyczny: Kontener KIE nie został zainicjowany.");
            return;
        }

        KieSession kSession = null;
        try {
            kSession = kieContainer.newKieSession("ksession-rules");

            kSession.insert(order);

            if (order.getPizzas() != null) {
                for (Pizza pizza : order.getPizzas()) {
                    kSession.insert(pizza);
                }
            }

            kSession.fireAllRules();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (kSession != null) {
                kSession.dispose();
            }
        }
    }
}