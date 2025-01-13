package apis

import base.BaseApi
import io.gatling.javaapi.core.ChainBuilder
import io.gatling.javaapi.core.Session
import io.gatling.javaapi.http.HttpProtocolBuilder
import static io.gatling.javaapi.core.CoreDsl.*

class DemoStoreApi extends BaseApi {

    enum Endpoint {
        LoadHomePage,
        LoadAboutUsPage,
        LoadCategoryPage,
        LoadLoginPage,
        LoadProductPage,
        AddProductToCart,
        ViewCart,
        CompleteCheckout,
        POSTLogin
    }

    DemoStoreApi(Endpoint endpoint) {
        this.apiName = 'ecom-demo-store'
        this.baseUrl = "https://demostore.gatling.io"
        this.reqMethod = ReqMethod.GET

        switch (endpoint) {
            case Endpoint.LoadHomePage:
                this.reqDesc = 'load home page'
                this.reqPath = '/'
                break
            case Endpoint.LoadAboutUsPage:
                this.reqDesc = 'load about-us page'
                this.reqPath = '/about-us'
                break
            case Endpoint.LoadCategoryPage:
                this.reqDesc = 'load category page'
                this.reqPath = '/category/{slug}'
                break
            case Endpoint.LoadLoginPage:
                this.reqDesc = 'load login page'
                this.reqPath = '/login'
                break
            case Endpoint.LoadProductPage:
                this.reqDesc = 'load product page'
                this.reqPath = '/product/{id}'
                break
            case Endpoint.AddProductToCart:
                this.reqDesc = 'add to cart product'
                this.reqPath = '/cart/add/{id}'
                break
            case Endpoint.ViewCart:
                this.reqDesc = 'view cart'
                this.reqPath = '/cart/view'
                break
            case Endpoint.CompleteCheckout:
                this.reqDesc = 'complete checkout'
                this.reqPath = '/cart/checkout'
                break
            case Endpoint.POSTLogin:
                this.reqMethod = ReqMethod.POST
                this.reqDesc = 'login'
                this.reqPath = '/login'
                break
        }
    }

    static HttpProtocolBuilder buildHttpProtocol() {
        new DemoStoreApi(null).buildingHttpProtocol([:]) //provide api specific headers, if any
    }

    static ChainBuilder loadHomePage(String saveTokenAs) {
        exec(
                new DemoStoreApi(Endpoint.LoadHomePage).buildHttpRequest()
                    .check(regex('<title>Gatling Demo-Store</title>'))
                    .check(css("meta[id='_csrf']", 'content').saveAs(saveTokenAs))
        )
    }

    static ChainBuilder loadAboutUsPage() {
        exec(
                new DemoStoreApi(Endpoint.LoadAboutUsPage).buildHttpRequest()
                    .check(substring('About Us'))
        )
    }

    static ChainBuilder login(String elKeyToken, String saveUserLoggedInAs) {
        feed(csv('data/lists/loginDetails.csv').circular())
            .exec(
                    new DemoStoreApi(Endpoint.LoadLoginPage).buildHttpRequest()
                            .check(substring("Username:")),
                    new DemoStoreApi(Endpoint.POSTLogin).tap {
                        reqDesc = "${reqDesc} as user #{userName}"
                    }.buildHttpRequest()
                        .formParamMap([
                                _csrf: elKeyToken,
                                username: '#{userName}',
                                password: '#{password}'
                        ]),
                    exec(session -> session.set(saveUserLoggedInAs, true))
            )
    }

    static ChainBuilder loadCategoryPage() {
        feed(csv('data/lists/categoryDetails.csv').random())
                .exec(
                        new DemoStoreApi(Endpoint.LoadCategoryPage).tap {
                            reqDesc = "${reqDesc} for #{categoryName}"
                        }.buildHttpRequest(['#{categorySlug}'])
                            .check(css("h2[id='CategoryName']").isEL('#{categoryName}'))
                )
    }

    static ChainBuilder loadProductPage() {
        feed(jsonFile('data/payloads/productDetails.json').random())
            .exec(
                    new DemoStoreApi(Endpoint.LoadProductPage).tap {
                        reqDesc = "${reqDesc} for #{name}"
                    }.buildHttpRequest(['#{slug}'])
                        .check(css("div[id='ProductDescription']").isEL('#{description}'))
            )
    }

    static ChainBuilder addProductToCart(String saveCartTotalAs) {
        exec(
                loadProductPage(),
                new DemoStoreApi(Endpoint.AddProductToCart).tap {
                    reqDesc = "${reqDesc} with id #{id}"
                }.buildHttpRequest(['#{id}'])
                        .check(substring("items in your cart")),
                exec { Session session ->
                    session.set(saveCartTotalAs, session.get(saveCartTotalAs) + session.get('price'))
                }
        )
    }

    static ChainBuilder viewCart(String userLoggedInSaveAs, String elKeyToken, String elKeyCartTotal) {
        doIf(session -> !session.getBoolean(userLoggedInSaveAs))
                .then(login(elKeyToken, userLoggedInSaveAs))
                .exec(
                        new DemoStoreApi(Endpoint.ViewCart).buildHttpRequest()
                                .check(css("[id='grandTotal']").isEL("\$${elKeyCartTotal}"))
                )
    }

    static ChainBuilder completeCheckout() {
        exec(
                new DemoStoreApi(Endpoint.CompleteCheckout).buildHttpRequest()
                        .check(substring("Thanks for your order! See you soon!"))
        )
    }
}