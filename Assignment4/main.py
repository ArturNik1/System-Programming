# This is a sample Python script.

# Press Shift+F10 to execute it or replace it with your code.
# Press Double Shift to search everywhere for classes, files, tool windows, actions, and settings.
import sqlite3
import sys

import Repository
import atexit


def read_from_file(path,repo):
    lines = []
    with open(path) as f:
        lines = f.readlines()

    count = 1
    line1 = lines[0].split(',')
    num_of_hats = int(line1[0])
    num_of_sup = int(line1[1])

    suppliers = []
    for line in lines[-num_of_sup:]:
        split = line.split(',')
        repo.Suppliers.insert(Supplier(split[0],split[1].rstrip()))

    hats = []
    for line in lines[1:num_of_hats + 1]:
        split = line.split(',')
        repo.hats.insert(Hat(*split))

def executeOrders(path, repo,outputPath):
    orderId=repo.Orders.lastId()
    if(orderId==None):
        orderId=1
    else:
        orderId=orderId+1
    orders = []
    with open(path) as ff:
        orders = ff.readlines()
    output=open(outputPath,'w')
    index = 0
    for order in orders:
        split = order.split(',')
        location = split[0]
        topping = split[1].rstrip()
        dic={"topping":topping}
        #hats=repo.hats.find_all()
        # hats=what_we_need(hats,topping)
        #print(hats[0].supplier)
        hats=repo.hats.find(**dic)
        if len(hats)>0:
            lower=getfirst(hats)
            lower.quantity= lower.quantity-1
            if lower.quantity==0:
                dic1={"id":lower.id}
                repo.hats.delete(**dic1)
            else:
                set={"quantity":lower.quantity}
                con = {"id": lower.id}
                repo.hats.update(set,con)
            repo.Orders.insert(Order(orderId, location, lower.id))
            sup=repo.Suppliers.find(**{"id":lower.supplier})
            output.write(lower.topping+','+sup[0].name+','+str(location))
            if len(orders) > index:
                output.write('\n')
            orderId = orderId + 1
            index = index + 1
    output.close()

def getfirst(hats):
    target=hats[0]
    for hat in hats:
        if hat.supplier<target.supplier:
            target=hat
    return target



class Order:
    def __init__(self, id, location,hat):
        self.id = id
        self.location = location
        self.hat = hat
class Supplier:
    def __init__(self,id,name):
        self.id = id
        self.name = name
    def __str__(self):
        print(self.id+" "+self.name)
class Hat:
    def __init__(self,id,topping,supplier,quantity):
        self.id = id
        self.topping = topping
        self.supplier=supplier
        self.quantity = quantity
    def __str__(self):
        print(str(self.id) +" "+ self.topping+" "+str(self.supplier)+" "+str(self.quantity))




# Press the green button in the gutter to run the script.
if __name__ == '__main__':
    repo = Repository._Repository(sys.argv[4])
    atexit.register(repo._close)
    repo.create_tables()
    read_from_file(sys.argv[1],repo)
    executeOrders(sys.argv[2], repo,sys.argv[3])


# See PyCharm help at https://www.jetbrains.com/help/pycharm/
