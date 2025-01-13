package simulations

import apis.DemoStoreApi
import base.BaseSimulation
import io.gatling.javaapi.http.HttpProtocolBuilder
import static io.gatling.javaapi.core.CoreDsl.*

@SuppressWarnings('unused')
class DemoStoreApiSimulation extends BaseSimulation {

    def chainBuilders = [
            exec(session -> session.setAll([cartTotal: 0.00, userLoggedIn: false])),
            printCurrentSessionDetails(['userLoggedIn', 'cartTotal']),
            DemoStoreApi.loadHomePage('csrfToken'),
            pause(1, 2),
            DemoStoreApi.loadAboutUsPage(),
            pause(1),
            DemoStoreApi.loadCategoryPage(),
            pause(1, 4),
            DemoStoreApi.addProductToCart('cartTotal'),
            printCurrentSessionDetails(['userLoggedIn', 'cartTotal']),
            pause(1),
            DemoStoreApi.viewCart('userLoggedIn', '#{csrfToken}', '#{cartTotal}'),
            pause(2),
            DemoStoreApi.completeCheckout(),
            pause(1),
            exec(session -> session.set('cartTotal', 0.00)), //reset cartTotal here
            printCurrentSessionDetails(['userLoggedIn', 'cartTotal']),
            DemoStoreApi.addProductToCart('cartTotal'),
            printCurrentSessionDetails(['userLoggedIn', 'cartTotal']),
            DemoStoreApi.viewCart('userLoggedIn', '#{csrfToken}', '#{cartTotal}'),
            DemoStoreApi.completeCheckout()
    ]

    def scn = scenario('load category with products, add product to cart, view cart and complete checkout as anonymous and logged-in user')
                .exec(chainBuilders)

    List<HttpProtocolBuilder> protocols = [
            DemoStoreApi.buildHttpProtocol()
    ]

    //simulation setup
    DemoStoreApiSimulation() {
        setUp(scn.injectOpen(atOnceUsers(1))).protocols(protocols)
    }
}