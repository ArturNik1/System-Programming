import inspect
class _Dao:
    def __init__(self, dto_type, conn):
        self._conn = conn
        self._dto_type = dto_type

        # dto_type is a class, its __name__ field contains a string representing the name of the class.
        self._table_name = dto_type.__name__.lower() + 's'

    def insert(self, dto_instance):
        ins_dict = vars(dto_instance)

        column_names = ','.join(ins_dict.keys())
        params = list(ins_dict.values())
        qmarks = ','.join(['?'] * len(ins_dict))

        stmt = 'INSERT INTO {} ({}) VALUES ({})'.format(self._table_name, column_names, qmarks)

        self._conn.execute(stmt, params)

    def find_all(self):
        c = self._conn.cursor()
        c.execute('SELECT * FROM {}'.format(self._table_name))
        return orm(c, self._dto_type)

    def find(self, **keyvals):
        column_names = keyvals.keys()
        params = list(keyvals.values())

        stmt = 'SELECT * FROM {} WHERE {}'.format(self._table_name, ' AND '.join([col + '=?' for col in column_names]))

        c = self._conn.cursor()
        c.execute(stmt,params)
        return orm(c, self._dto_type)

    def delete(self, **keyvals):
        column_names = keyvals.keys()
        params = list(keyvals.values())

        stmt = 'DELETE FROM {} WHERE {}'.format(self._table_name, ' AND '.join([col + '=?' for col in column_names]))

        c = self._conn.cursor()
        c.execute(stmt, params)

    def update(self, set_values, cond):
        set_column_names = set_values.keys()
        set_params = set_values.values()

        cond_column_names = cond.keys()
        cond_params = cond.values()

        params = list(set_params) + list(cond_params)

        stmt = 'UPDATE {} SET {} WHERE {}'.format(self._table_name,
                                                  ', '.join([set + '=?' for set in set_column_names]),
                                                  ' AND '.join([cond + '=?' for cond in cond_column_names]))

        self._conn.execute(stmt, params)
    def lastId(self):
        stmt='SELECT max(id) FROM {}'.format(self._table_name)
        c = self._conn.cursor()
        c.execute(stmt)
        return c.fetchone()[0]
def orm (cursor,dto_type):
    args=inspect.getargspec(dto_type.__init__).args
    args=args[1:]
    col_names=[column[0] for column in cursor.description]
    col_mapping = [col_names.index(arg) for arg in args]
    l=[row_map(row, col_mapping, dto_type) for row in cursor.fetchall()]
    return l

def row_map(row,col_mapping,dto_type):
    ctor_args = [row[idx] for idx in col_mapping]
    return dto_type(*ctor_args)