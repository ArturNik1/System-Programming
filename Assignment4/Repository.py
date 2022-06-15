from Dao import _Dao
import main
import sqlite3

class _Repository:
    def __init__(self,path):
        self._conn = sqlite3.connect(path)
        self.hats = _Dao(main.Hat, self._conn)
        self.Suppliers = _Dao(main.Supplier,self._conn)
        self.Orders = _Dao(main.Order,self._conn)

    def _close(self):
        self._conn.commit()
        self._conn.close()

    def create_tables(self):
        self._conn.execute("""CREATE TABLE IF NOT EXISTS suppliers(
                        id INTEGER PRIMARY KEY,
                        name STRING NOT NULL )""")
        self._conn.execute("""CREATE TABLE IF NOT EXISTS hats(
                         id INTEGER PRIMARY KEY,
                         topping STRING NOT NULL,
                         supplier INTEGER REFERENCES suppliers (id),
                         quantity INTEGER NOT NULL)""")
        self._conn.execute("""CREATE TABLE IF NOT EXISTS orders(
                         id PRIMARY KEY,
                         location STRING NOT NULL,
                         hat INTEGER REFERENCES hats(id))""")

    # the repository singleton


