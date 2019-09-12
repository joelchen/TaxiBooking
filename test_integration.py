import requests
import json

server_url = 'http://localhost:8080/api'

def tick(n=1):
    for _ in range(n):
        response = requests.post(server_url + '/tick')
        print(response.content)

def reset():
    response = requests.put(server_url + '/reset')
    print(response.content)

def book(source, destination, expected):
    data = {'source': source, 'destination': destination}
    head = {'Content-type': 'application/json', 'Accept': 'application/json'}
    response = requests.post(
        server_url + '/book',
        headers=head,
        data=json.dumps(data)
    )
    print(response.content)
    parsed = ''
    try:
        parsed = json.loads(response.content)
    except ValueError:
        print('Cannot decode JSON')
    check_car(expected, parsed)

def check_car(expected, actual):
    test_string = 'expected: {}, actual: {}'.format(expected, actual)
    errors = []

    if 'car_id' in actual and 'total_time' in actual and 'car_id' in expected and 'total_time' in expected:
        if actual['car_id'] != expected['car_id']:
            errors.append('wrong car_id')
        if actual['total_time'] != expected['total_time']:
            errors.append('wrong total_time')

    if len(errors) > 0 or expected != actual:
        print('Failed {} - reason: {}'.format(test_string, ', '.join(errors)))
    else:
        print('Success - {}'.format(test_string))

if __name__ == '__main__':
    reset()
    book({'x': 1, 'y': 0}, {'x': 1, 'y': 1}, {'car_id': 1, 'total_time': 2})
    book({'x': 1, 'y': 1}, {'x': 3, 'y': 3}, {'car_id': 2, 'total_time': 6})
    tick()
    book({'x': -1, 'y': 1}, {'x': 0, 'y': 1}, {'car_id': 3, 'total_time': 3})
    book({'x': 1, 'y': 1}, {'x': -1, 'y': -1}, '')
    tick()
    book({'x': 2, 'y': 3}, {'x': -5, 'y': -2}, {'car_id': 1, 'total_time': 15})
    tick()
    book({'x': -1, 'y': -1}, {'x': 1, 'y': 1}, '')
    tick(3)
    book({'x': -2, 'y': 3}, {'x': -2, 'y': -3}, {'car_id': 3, 'total_time': 10})
    reset()
    book({'x': 1, 'y': 0}, {'x': 1, 'y': 1}, {'car_id': 1, 'total_time': 2})
    book({'x': 1, 'y': 1}, {'x': 5, 'y': 5}, {'car_id': 2, 'total_time': 10})
    tick()
    book({'x': -1, 'y': 1}, {'x': 5, 'y': 2}, {'car_id': 3, 'total_time': 9})
    book({'x': 1, 'y': 1}, {'x': -1, 'y': -1}, '')