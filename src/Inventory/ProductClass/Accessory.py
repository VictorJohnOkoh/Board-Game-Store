# A data holding class for an Accessory product to be passed to the Database manager

class Accessory:
    def __init__(self, id: int, type: str, name: str, price: float, stock: int, purchaseCost: float, compatibility: str):
        self.id = id
        self.type = type
        self.name = name
        self.price = price
        self.stock = stock
        self.cost = purchaseCost
        self.compatibility = compatibility

    def getID(self):
        return self.id
    def getType(self):
        return self.type
    def getName(self):
        return self.name
    def getPrice(self):
        return self.price
    def getStock(self):
        return self.cost
    def getPlayers(self):
        return self.compatibility