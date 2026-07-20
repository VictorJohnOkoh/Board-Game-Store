import os
import sqlite3
import shutil


_DB_PATH = None
_BACKUP_DIR = None
_BACKUP_PATH = None

def init_paths():
    global _DB_PATH, _BACKUP_DIR, _BACKUP_PATH
    if 'db_path' in globals() and db_path:
        _DB_PATH = db_path
    else:
        _DB_PATH = os.path.join(os.getcwd(), 'src', 'Inventory', 'StoreData.db')
    script_dir = os.path.dirname(_DB_PATH)
    _BACKUP_DIR = os.path.join(script_dir, 'backups')
    _BACKUP_PATH = os.path.join(_BACKUP_DIR, 'StoreData_backup.db')

def rollback():
    shutil.copy2(_BACKUP_PATH, _DB_PATH)

def create_backup():
    if not os.path.exists(_BACKUP_DIR):
        os.makedirs(_BACKUP_DIR)
    shutil.copy2(_DB_PATH, _BACKUP_PATH)

init_paths()

# stops redundant connections in each function
try:
    conn = sqlite3.connect(_DB_PATH)
    cursor = conn.cursor()
except sqlite3.OperationalError:
    print("Couldn't open the database")
except FileNotFoundError:
    rollback()


def get_user_role(ID: int):
    """Returns the user's role"""

    query = "SELECT role FROM UserRole WHERE userid = ?"
    cursor.execute(query, (ID,))
    rows = cursor.fetchall()
    role = rows[0][0]

    return role


def get_user_details():
    """Returns the details of all users of the system
    In the format: userid | name | house number | postcode | city | role
    """

    query = "SELECT UserDetails.userid, name, housenum, postcode, city, role FROM main.UserDetails LEFT JOIN main.UserRole ON UserDetails.userid = UserRole.userid"
    cursor.execute(query)
    rows = cursor.fetchall()
    result = ""
    for row in rows:
        line = f"{row[0]};{row[1]};{row[2]};{row[3]};{row[4]};{row[5]}"
        result += line
        if rows.index(row) < len(rows)-1:
            result += ','

    return result


def get_user_address(ID: int):
    """Returns the userid, house number, postcode and city of a user in an array
    0 - userid
    1 - house number
    2 - postcode
    3 - city
    """

    query = "SELECT userid, housenum, postcode, city FROM UserDetails WHERE userid = ?"
    cursor.execute(query, (ID,))
    rows = cursor.fetchall()
    address = [rows[0][0], rows[0][1], rows[0][2], rows[0][3]]

    return address

def get_admin_products(ID: int):
    """Returns all information about all board games and accessories stored in the store database"""
    if get_user_role(ID) != 'admin':
        return "You don't have the necessary permissions"


    query =  "SELECT BoardGame.id, 'boardgame', genre, name, price, quantity, pcost, noplayers FROM main.BoardGame LEFT JOIN main.BoardGamePlayers ON BoardGame.id = BoardGamePlayers.id UNION SELECT Accessory.id, 'accessory', type, name, price, quantity, pcost, compatibility FROM main.Accessory LEFT JOIN main.AccessoryCompatibility AC on Accessory.id = AC.id ORDER BY price DESC"
    cursor.execute(query)
    rows = cursor.fetchall()
    result = ""
    for row in rows:
        if row[1] == 'boardgame':
            line = f"ProductID: {row[0]}\t | Category: {row[1]}\t | Type: {row[2]:<13s}\t | Name: {row[3]:<27s}\t | Price: {row[4]:.2f}\t | Quantity: {row[5]}\t | Cost: {row[6]:.2f}\t | No. Players: {row[7]}\t\n"
        else:
            line = f"ProductID: {row[0]}\t | Category: {row[1]}\t | Type: {row[2]:<13s}\t | Name: {row[3]:<27s}\t | Price: {row[4]:.2f}\t | Quantity: {row[5]}\t | Cost: {row[6]:.2f}\t | Compatibility: {row[7]}\t\n"
        result += line

    return result


def get_products():
    """Returns information viewable by customers about all board games and accessories stored in the store database"""

    query = "SELECT BoardGame.id, 'boardgame', genre, name, price, quantity, pcost, noplayers FROM main.BoardGame LEFT JOIN main.BoardGamePlayers ON BoardGame.id = BoardGamePlayers.id UNION SELECT Accessory.id, 'accessory', type, name, price, quantity, pcost, compatibility FROM Accessory LEFT JOIN AccessoryCompatibility AC on Accessory.id = AC.id ORDER BY price DESC"
    cursor.execute(query)
    rows = cursor.fetchall()
    result = ""
    for row in rows:
        if row[1] == 'boardgame':
            line = f"ProductID: {row[0]}\t | Category: {row[1]}\t | Type: {row[2]:<13s}\t | Name: {row[3]:<27s}\t | Price: {row[4]:.2f}\t | Quantity: {row[5]}\t | No. Players: {row[7]}\t\n"
        else:
            line = f"ProductID: {row[0]}\t | Category: {row[1]}\t | Type: {row[2]:<13s}\t | Name: {row[3]:<27s}\t | Price: {row[4]:.2f}\t | Quantity: {row[5]}\t | Compatibility: {row[7]}\t\n"
        result += line

    return result


def filter_product_compatibility(search: str):
    """Filters the products by their compatibility"""

    query = "SELECT Accessory.id, 'accessory',type, name, price, quantity, pcost, compatibility FROM main.Accessory LEFT JOIN AccessoryCompatibility ON Accessory.id = AccessoryCompatibility.id WHERE compatibility LIKE ?;"
    cursor.execute(query, (f"%{search}%",))
    rows = cursor.fetchall()
    result = ""
    for row in rows:
        line = f"ProductID: {row[0]}\t | Category: {row[1]}\t | Type: {row[2]:<13s}\t | Name: {row[3]:<27s}\t | Price: {row[4]:.2f}\t | Quantity: {row[5]}\t | Compatibility: {row[7]}\t\n"
        result += line

    return result


def filter_product_id(search: int):
    """Filters the products by their ID"""

    query = "SELECT Boardgame.id, 'boardgame', genre, name, price, quantity, pcost, noplayers FROM main.BoardGame LEFT JOIN main.BoardGamePlayers BGP on BoardGame.id = BGP.id WHERE BoardGame.id LIKE ? UNION SELECT Accessory.id, 'accessory', type, name, price, quantity, pcost, compatibility FROM main.Accessory LEFT JOIN main.AccessoryCompatibility AC on Accessory.id = AC.id WHERE Accessory.id LIKE ?;"
    cursor.execute(query, (f"%{search}%", f"%{search}%",))
    rows = cursor.fetchall()
    result = ""
    for row in rows:
        if row[1] == 'boardgame':
            line = f"ProductID: {row[0]}\t | Category: {row[1]}\t | Type: {row[2]:<13s}\t | Name: {row[3]:<27s}\t | Price: {row[4]:.2f}\t | Quantity: {row[5]}\t | No. Players: {row[7]}\t\n"
        else:
            line = f"ProductID: {row[0]}\t | Category: {row[1]}\t | Type: {row[2]:<13s}\t | Name: {row[3]:<27s}\t | Price: {row[4]:.2f}\t | Quantity: {row[5]}\t | Compatibility: {row[7]}\t\n"
        result += line

    return result


def update_stock(basket_data: str):
    """Updates stock for multiple products from a basket string.
    To be used after a basket has been paid for
    Input format: 'id1:amount1;id2:amount2;...'
    Returns: 'Success' or error message if any item has insufficient stock
    """
    if not basket_data:
        return "Success"

    items = basket_data.split(';')
    create_backup()

    for item in items:
        if ':' not in item:
            continue
        parts = item.split(':')
        try:
            pid = int(parts[0])
            amount = int(parts[1])
        except ValueError:
            continue

        query_check = "SELECT quantity FROM main.BoardGame WHERE id=? UNION SELECT quantity FROM main.Accessory WHERE id=?"
        cursor.execute(query_check, (pid, pid))
        result = cursor.fetchone()

        if result is None or result[0] < amount:
            query_name = "SELECT name FROM main.BoardGame WHERE id=? UNION SELECT name FROM main.Accessory WHERE id=?"
            cursor.execute(query_name, (pid, pid))
            item_name = cursor.fetchone()
            conn.rollback()
            return f"Not enough of the {item_name[0]} is in stock"

        query_boardgame = "UPDATE BoardGame SET quantity=quantity-? WHERE id=? AND quantity>=?"
        cursor.execute(query_boardgame, (amount, pid, amount))

        if cursor.rowcount == 0:
            query_accessory = "UPDATE Accessory SET quantity=quantity-? WHERE id=? AND quantity>=?"
            cursor.execute(query_accessory, (amount, pid, amount))

    conn.commit()
    return "Success"

def check_stock(amount: int, ID: int):
    """checks if there is enough of the product to be bought in stock returns true if there's enough stock otherwise it returns false"""

    query = "SELECT BoardGame.quantity From BoardGame WHERE id=? UNION SELECT Accessory.quantity FROM Accessory WHERE id=?"
    cursor.execute(query, (ID, ID))
    current_amount = cursor.fetchone()
    if current_amount[0] == 0:
        return False
    elif current_amount - amount < 0:
        return False
    else:
        return True


def add_board_game(productID: int, name: str, genretype: str, price: float, stock: int, purchase_cost: float, players: int):
    """Adds a new board game to the BoardGame and BoardGamePlayers tables"""

    create_backup()
    try:
        query1 = 'INSERT INTO main.BoardGame (id, genre, name, price, quantity, pcost) VALUES (?, ?, ?, ?, ?, ?);'
        query2 = 'INSERT INTO main.BoardGamePlayers(id, noplayers) VALUES (?, ?);'
        cursor.execute(query1, (productID, genretype, name, price, stock, purchase_cost))
        cursor.execute(query2, (productID, players))
        conn.commit()

        return "Success"
    except sqlite3.IntegrityError:
        return "Product with that ID already exists"


def add_accessory(productID: int, name: str, genretype: str, price: float, stock: int, purchase_cost: float, compatibility: str):
    """Adds a new accessory to the Accessory and AccessoryCompatibility  tables"""

    create_backup()
    try:
        query1 = 'INSERT INTO main.Accessory (id, type, name, price, quantity, pcost) VALUES (?, ?, ?, ?, ?, ?);'
        query2 = 'INSERT INTO main.AccessoryCompatibility (id, compatibility) VALUES (?, ?);'
        cursor.execute(query1, (productID, genretype, name, price, stock, purchase_cost))
        cursor.execute(query2, (productID, compatibility))
        conn.commit()

        return "Success"
    except sqlite3.IntegrityError:
        return "Product with that ID already exists"

def load_users():
    """Returns all users in the same semicolon-delimited format as UserAccount.txt"""

    query = "SELECT UserDetails.userid, name, role FROM main.UserDetails LEFT JOIN main.UserRole ON UserDetails.userid = UserRole.userid"
    cursor.execute(query)
    rows = cursor.fetchall()
    result = ""
    i = 0
    for row in rows:
        line = f"{i}) {row[0]} | {row[1]} | {row[2]} | {row[3]}\n"
        result += line
        
    return result

def get_product_by_id(ID: int) -> str:
    """Returns a semicolon-delimited row for a product by ID, or 'NOT_FOUND'."""
    query = "SELECT BoardGame.id, 'boardgame', genre, name, price, quantity, pcost, noplayers FROM BoardGame LEFT JOIN BoardGamePlayers ON BoardGame.id = BoardGamePlayers.id WHERE BoardGame.id=? UNION SELECT Accessory.id, 'accessory', type, name, price, quantity, pcost, compatibility FROM Accessory LEFT JOIN AccessoryCompatibility ON Accessory.id = AccessoryCompatibility.id WHERE Accessory.id=?"
    cursor.execute(query, (ID, ID))
    row = cursor.fetchone()
    if row:
        return f"{row[0]};{row[1]};{row[2]};{row[3]};{row[4]};{row[5]};{row[6]};{row[7]}"
    return "NOT_FOUND"


def check_basket_stock(basket_data: str) -> str:
    """Checks stock for multiple items. Input: 'id1:amount1;id2:amount2;...' Output: 'OK' or ';'-delimited out-of-stock names."""
    if not basket_data:
        return "OK"

    items = basket_data.split(';')
    out_of_stock_names = []

    for item in items:
        if ':' not in item:
            continue
        parts = item.split(':')
        try:
            pid = int(parts[0])
            amount = int(parts[1])
        except ValueError:
            continue

        # Check stock
        query = "SELECT quantity FROM main.BoardGame WHERE id=? UNION SELECT quantity FROM main.Accessory WHERE id=?"
        cursor.execute(query, (pid, pid))
        result = cursor.fetchone()

        if result and result[0] >= amount:
            continue  # Enough stock

        # Not enough stock, get name
        query_name = "SELECT name FROM main.BoardGame WHERE id=? UNION SELECT name FROM main.Accessory WHERE id=?"
        cursor.execute(query_name, (pid, pid))
        name_result = cursor.fetchone()
        if name_result:
            out_of_stock_names.append(name_result[0])

    if not out_of_stock_names:
        return "OK"
    return ';'.join(out_of_stock_names)


def check_product_conflict(name: str, ID: int) -> str:
    """Checks for duplicate name or ID. Returns 'NONE', 'NAME', 'ID', or 'BOTH'."""
    has_name_conflict = False
    has_id_conflict = False

    # Check ID
    query_id = "SELECT id FROM main.BoardGame WHERE id=? UNION SELECT id FROM main.Accessory WHERE id=?"
    cursor.execute(query_id, (ID, ID))
    if cursor.fetchone():
        has_id_conflict = True

    # Check Name (case-insensitive)
    query_name = "SELECT name FROM main.BoardGame WHERE LOWER(name)=LOWER(?) UNION SELECT name FROM main.Accessory WHERE LOWER(name)=Lower(?)"
    cursor.execute(query_name, (name, name))
    if cursor.fetchone():
        has_name_conflict = True

    if has_id_conflict and has_name_conflict:
        return "BOTH"
    elif has_id_conflict:
        return "ID"
    elif has_name_conflict:
        return "NAME"
    else:
        return "NONE"


# Closes the connection to the database at the end of the program
def close_connection():
    global conn, cursor
    if cursor:
        cursor.close()
    if conn:
        conn.close()
    return "Database connection closed cleanly"

def check_db():
    tables = ['BoardGame', 'BoardGamePlayers', 'Accessory', 'AccessoryCompatibility', 'UserDetails', 'UserRole']
    for table in tables:
        try: # checks if all tables in the database
            query = 'SELECT * From ?'
            cursor.execute(query, (table,))
        except sqlite3.OperationalError:
            print("Database Corrupted: some table(s) don't exist")

# Structure to run functions by passing JSON data
#
# def main():
#
#     # checks if a function argument was passed
#     if len(sys.argv) < 2:
#         print("Error no function targeted")
#         sys.exit(1)
#
#     target_function = sys.argv[1]
#     input_data = sys.stdin.read()
#     if not input_data:
#         print("Error: no JSON received via stdin")
#         return
#     data = json.loads(input_data)
#
#     # runs the function associated to the argument passed and takes in arguments piped from Java
#     if target_function == "getUserRole":
#         getUserRole(int(data[0]))
#     elif target_function == "getUserDetails":
#         getUserDetails()
#     elif target_function == "getUserAddress":
#         getUserAddress(int(data[0]))
#     elif target_function == "getAdminProducts":
#         getAdminProducts(int(data[0]))
#     elif target_function == "getProducts":
#         getProducts()
#     elif target_function == "filterProductCompatibility":
#         filterProductCompatibility(data[0])
#     elif target_function == "updateStock":
#         updateStock(int(data['id']), int(data['stock']), data['category'])
#     elif target_function == "addBoardGame":
#         addBoardGame(int(data['id']), data['name'], data['type'], float(data['price']), int(data['stock']), float(data['cost']), int(data['players']))
#     elif target_function == "addAccessory":
#         addAccessory(int(data['id']), data['name'], data['type'], float(data['price']), int(data['stock']), float(data['cost']), data['compatibility'])
#     elif target_function == "loadUsers":
#         loadUsers()
#     elif target_function == " close_connection":
#         close_connection()
#     else:
#         print(f"Error: unknown function name {target_function}")
#         sys.exit(1)
#
# if __name__ == "__main__":
#     main()