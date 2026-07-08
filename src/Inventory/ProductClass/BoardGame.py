# Basically a data storage clas for a boardgame product to be passed to the database manager

class BoardGame:
    def __init__(self, id: int, type: str, name: str, price: float, stock: int, purchaseCost: float, players: int):
        self.id = id
        self.type = type
        self.name = name
        self.price = price
        self.stock = stock
        self.cost = purchaseCost
        self.players = players

    def getID(self):
        return self.id
    def getType(self):
        return self.type
    def getName(self):
        return self.name
    def getPrice(self):
        return self.price
    def getStock(self):
        return self.stock
    def getCost(self):
        return self.cost
    def getPlayers(self):
        return self.players

