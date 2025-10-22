package ui.elements;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.By;

public class BaseElement {
    protected final SelenideElement element;

    public BaseElement(SelenideElement element) {
        this.element = element;
    }

    protected SelenideElement find(By selector) {
        return element.find(selector);
    }

    protected SelenideElement find(String cssSelector) {
        return element.find(cssSelector);
    }

    protected ElementsCollection findAll(By cssSelector) {
        return element.findAll(cssSelector);
    }

    protected ElementsCollection findAll(String cssSelector) {
        return element.findAll(cssSelector);
    }

}
