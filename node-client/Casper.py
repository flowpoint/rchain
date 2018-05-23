'''RChain casper client

Usage:
  $ export FLASK_APP=Casper.py
  $ flask run

  Navigate to http://localhost:5000/api/block/<block_hash>

We assume the RChain node is running and that it is listening on port
5000. Double-check that you see this message in the logs:

  Server started, listening on 50000

The output should be something like:


Tested with rnode-assembly-0.3.1.jar from commit 2424b43caae May 22 2018.

'''

from __future__ import print_function

# cribbed from https://grpc.io/docs/tutorials/basic/python.html
import CasperMessage_pb2
import CasperMessage_pb2_grpc

from sys import argv, stdout
from grpc import insecure_channel
from flask import Flask
from flask import jsonify
import json

def buildCasperCh(argv, stdout, insecure_channel,
         host='127.0.0.1',
         port=50000):
    channel = insecure_channel('%s:%s' % (host, port))
    return CasperMessage_pb2_grpc.DeployServiceStub(channel)

app = Flask(__name__)
casperCh = buildCasperCh(argv, stdout, insecure_channel)

@app.route("/api/block/<block_hash>")
def block(block_hash):
    req = CasperMessage_pb2.BlockQuery(hash=block_hash)
    output = casperCh.showBlock(req)
    output_dict = {}
    for field in output.ListFields():
        field_name = field[0].name
        field_value = str(field[1])
        output_dict.update({field_name: field_value})
    return jsonify(output_dict)