import os
import sqlite3

# TODO - Add function that works with load products to replace use of text file
#
DB_Path = r'src\Inventory\StoreData.db'

# stops redundant connections in each function
try:
    print(f"Your CWD is: {os.getcwd()}")
    print(f"Python is looking for the file at: {os.path.abspath('StoreData.db')}")
    print(f"Does the file actually exist there? {os.path.exists('StoreData.db')}")
    conn = sqlite3.connect(DB_Path)
    cursor = conn.cursor()
except sqlite3.OperationalError:
    print("Couldn't open the database")

def getUserRole(ID: int):
    """Returns the user's role"""

    query = "SELECT role FROM UserRole WHERE userid = ?"
    cursor.execute(query, (ID,))
    rows = cursor.fetchall()
    role = rows[0]

    return role


def getUserDetails():
    """Prints the details of all users of the system"""

    query = "SELECT UserDetails.userid, name, housenum, postcode, city, role FROM main.UserDetails LEFT JOIN main.UserRole ON UserDetails.userid = UserRole.userid"
    cursor.execute(query)
    rows = cursor.fetchall()
    result = ""
    for row in rows:
        line = f"UserID: {row[0]}\t | Name: {row[1]}\t | House Number: {row[2]:<3}\t | Postcode: {row[3]:<8}\t | City: {row[4]:<15}\t | Role: {row[5]}\t\n"
        result += line

    return result


def getUserAddress(ID: int):
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

def getAdminProducts(ID: int):
    """Returns all information about all board games and accessories stored in the store database"""
    if getUserRole(ID) == 'admin':
        print()
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


def getProducts():
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


def filterProductCompatibility(search: str):
    """Filters the products by their compatibility"""

    query = "SELECT Accessory.id, 'accessory',type, name, price, quantity, pcost, compatibility FROM main.Accessory LEFT JOIN AccessoryCompatibility ON Accessory.id = AccessoryCompatibility.id WHERE compatibility LIKE ?;"
    cursor.execute(query, (f"%{search}%",))
    rows = cursor.fetchall()
    result = ""
    for row in rows:
        line = f"ProductID: {row[0]}\t | Category: {row[1]}\t | Type: {row[2]:<13s}\t | Name: {row[3]:<27s}\t | Price: {row[4]:.2f}\t | Quantity: {row[5]}\t | Compatibility: {row[7]}\t\n"
        result += line

    return result


def filterProductID(search: int):
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


def updateStock(amount: int, ID: int, category: str):
    """Updates the stock of a product after its purchase"""

    if category == 'boardgame':
        query = "UPDATE BoardGame SET main.BoardGame.quantity=(quantity-?) WHERE main.BoardGame.id=?"
        cursor.execute(query, (amount, ID))
    else:
        query = "UPDATE BoardGame SET main.Accessory.quantity=(quantity-?) WHERE main.Accessory.id=?"
        cursor.execute(query, (amount, ID))

    return "Success"


def addBoardGame(productID: int, name: str, genretype: str, price: float, stock: int, purchase_cost: float, players: int):
    """Adds a new board game to the BoardGame and BoardGamePlayers tables"""
    try:
        query1 = 'INSERT INTO main.BoardGame (id, genre, name, price, quantity, pcost) VALUES (?, ?, ?, ?, ?, ?);'
        query2 = 'INSERT INTO main.BoardGamePlayers(id, noplayers) VALUES (?, ?);'
        cursor.execute(query1, (productID, genretype, name, price, stock, purchase_cost))
        cursor.execute(query2, (productID, players))
        conn.commit()

        return "Success"
    except sqlite3.IntegrityError:
        return "Product with that ID already exists"


def addAccessory(productID: int, name: str, genretype: str, price: float, stock: int, purchase_cost: float, compatibility: str):
    """Adds a new accessory to the Accessory and AccessoryCompatibility  tables"""
    try:
        query1 = 'INSERT INTO main.Accessory (id, type, name, price, quantity, pcost) VALUES (?, ?, ?, ?, ?, ?);'
        query2 = 'INSERT INTO main.AccessoryCompatibility (id, compatibility) VALUES (?, ?);'
        cursor.execute(query1, (productID, genretype, name, price, stock, purchase_cost))
        cursor.execute(query2, (productID, compatibility))
        conn.commit()

        return "Success"
    except sqlite3.IntegrityError:
        return "Product with that ID already exists"

def loadUsers():
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

# Closes the connection to the database at the end of the program
def close_connection():
    global conn, cursor
    if cursor:
        cursor.close()
    if conn:
        conn.close()
    return "Database connection closed cleanly"

def checkDB():
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