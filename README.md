# Board Game Store

A desktop shop for board games and accessories. You log in as an **admin** or a **customer**,
and get a different set of screens depending on which.

Written in Java with a JavaFX interface. The data lives in SQLite, and every database query
is written in Python — Java calls into it through an embedded interpreter.

---

## What you can do

**As a customer**

| Screen | What it does |
|---|---|
| View Products | Browse everything in stock, search by product ID or compatibility, add to basket from the row |
| Pay | See your basket, remove lines, pick PayPal or card, and pay |
| Clear Basket | Empty the basket |

**As an admin**

| Screen | What it does |
|---|---|
| View All Products | The full stock list, including the purchase cost |
| Add New Product | Add a board game or an accessory |
| Rollback Database | Restore the last backup |

Customers never see the purchase cost. That is enforced in the SQL, not just hidden in the
interface — the customer queries do not select the `pcost` column at all.

> **Note:** picking a user is not a login. There are no passwords. This is coursework, not a
> real shop, and payment details are validated for shape only — nothing is ever charged.

---

## Running it

**The easy way.** Download a bundle for your platform from the
[Actions](../../actions) artifacts, unzip it, and run `Scripts/run.bat` (Windows) or
`Scripts/run.sh` (Linux/macOS). The bundle carries its own Python, so nothing needs installing.

**From source.** You need JDK 25 and Maven.

```bash
mvn clean package      # build
mvn test               # run the tests (51 of them)
```

Then run `gui.app.MainApp` from your IDE. There is also a command line version at
`CLIbasis.CLIbasis.Main` which does the same jobs in a text menu.

---

## How it fits together

Four layers. Each one only talks to the one below it.

```mermaid
flowchart TD
    subgraph UI["Interface"]
        GUI["JavaFX screens<br/>gui.app.controllers"]
        CLI["Text menus<br/>CLIbasis"]
    end

    subgraph Domain["Domain — the rules"]
        Users["Users<br/>Customer, Admin, newBasket"]
        Inventory["Inventory<br/>Product, BoardGame, Accessory"]
        Payment["Payment<br/>PayPal, CreditCard, Receipt"]
    end

    Bridge["JavaPythonBridge<br/>one method name per query"]
    Python["DatabaseManager.py<br/>every SQL statement"]
    DB[("SQLite<br/>StoreData.db")]

    GUI --> Domain
    CLI --> Domain
    Domain --> Bridge
    Bridge -->|JEP| Python
    Python --> DB
```

The important part is that **the GUI and the CLI share everything below them**. A basket rule
or a payment check is written once and both interfaces get it. That is why the payment classes
take plain values instead of reading from the console — a `Scanner` would have locked them to
the text version.

### Java to Python

Java does not write SQL. It asks the Python module for what it wants, by name.

```mermaid
sequenceDiagram
    participant App as Java
    participant Loader as PythonScriptLoader
    participant Jep as JEP interpreter
    participant Py as DatabaseManager.py

    App->>Loader: start up
    Loader->>Loader: copy the script out of the jar into temp/
    Loader-->>Jep: run it
    Note over Loader: the copy is deleted again when the app closes

    App->>Jep: invoke("get_products_raw")
    Jep->>Py: call the function
    Py-->>App: 3#59;boardgame#59;strategy#59;Catan#59;34.99#59;12#59;4
```

The script has to be copied to a real file first, because an embedded interpreter cannot run
a script that is still inside a jar. It sits next to the `data` folder, which is how the Python
finds `StoreData.db` on its own — purely from its own location.

Results come back as text. One row per line, fields split by `;`.

---

## The domain

```mermaid
classDiagram
    class User {
        <<abstract>>
        +getName()
        +getAddress()
    }
    class Customer {
        +basket
        +checkout(PaymentMethod) Receipt
    }
    class newBasket {
        +addShopping(id) AddResult
        +removeProduct(product, amount)
        +getTotalPrice() double
    }
    class Product {
        <<abstract>>
        +getPrice()
        +getQuantityInStock()
    }
    class PaymentMethod {
        <<interface>>
        +processPayment(total, address) Receipt
    }
    class Receipt {
        <<abstract>>
        +save(items) Path
    }

    User <|-- Customer
    User <|-- Admin
    Customer *-- newBasket
    newBasket o-- Product
    Product <|-- BoardGame
    Product <|-- Accessory
    PaymentMethod <|.. PayPal
    PaymentMethod <|.. CreditCard
    Receipt <|-- PayPalReceipt
    Receipt <|-- CreditCardReceipt
```

A few decisions worth knowing:

- **The basket lives on the `Customer` object.** Screens pass that same object between
  themselves. Reloading the user from the database would hand back an empty basket.
- **`addShopping` returns a result, it does not print.** `ADDED`, `NOT_FOUND`,
  `INVALID_AMOUNT` or `INSUFFICIENT_STOCK` — so both interfaces can explain a failure in
  their own way.
- **Payment cannot fail.** There is no real gateway. Only the details are checked.

### Paying

```mermaid
sequenceDiagram
    participant UI as Payment screen
    participant C as Customer
    participant M as PaymentMethod
    participant R as Receipt
    participant DB as Database

    UI->>C: checkout(method)
    C->>M: processPayment(total, address)
    M-->>C: receipt
    C->>R: save(items)
    Note right of R: markdown file in data/receipts
    C->>DB: update stock
    alt stock updated
        C->>C: empty the basket
        C-->>UI: receipt
    else update failed
        C-->>UI: CheckoutException
        Note right of C: basket is kept
    end
```

If the stock update fails, the basket is **not** emptied. Better to make someone pay again
than to take their money and lose their order.

Every sale writes a receipt to `data/receipts/`, named for the time it happened, like
`2026-08-06_15-16-27.md`:

```markdown
# Receipt - 06/08/2026 15:16

| Product | Qty | Total |
|---------|-----|-------|
| Kingdoms of Valor | 1 | £45.00 |

£45.00 paid by Credit Card 101234 on 06/08/2026. Billing Address: 100 CV1 2GT Coventry
```

---

## Project layout

```
src/main/java/
  gui/app/            JavaFX entry point and screen controllers
  CLIbasis/           text menus
  Users/              User, Admin, Customer, newBasket
  Inventory/          Product, BoardGame, Accessory
  Payment/            payment methods and receipts
  Bridge/             the Java to Python link
src/main/resources/
  DatabaseManager.py  every SQL query in the project
  controllers/        FXML screen layouts and the stylesheet
src/test/java/        unit tests for the rules
data/StoreData.db     the database
packaging/            launcher scripts
```

## Database

SQLite. Six tables — products are split so that the parts unique to each kind live separately.

```mermaid
erDiagram
    BoardGame ||--|| BoardGamePlayers : "player count"
    Accessory ||--|| AccessoryCompatibility : "works with"
    UserDetails ||--|| UserRole : "admin or customer"
```

`Rollback Database` restores `data/backups/`, which is written before anything changes stock.

## Building the releases

A GitHub Actions workflow builds one bundle per platform. It cannot be a single download,
because the embedded interpreter is a native library — Windows, Linux and macOS each need
their own. Each bundle is smoke tested before it is published: the workflow starts the app
and checks it reaches the menu.

## Licence

Apache 2.0. See [LICENSE](LICENSE). Each release bundle ships the licences of the things it
carries — Python, JavaFX and JEP — in its own `licenses/` folder.
