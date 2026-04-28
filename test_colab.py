import urllib.request, json, pickle, io
import os
from sklearn.naive_bayes import GaussianNB
from sklearn.preprocessing import LabelEncoder
import numpy as np

BASE = "http://localhost/fastapi"

def post(path, body=None, content_type="application/json"):
    data = json.dumps(body).encode() if body else None
    req = urllib.request.Request(f"{BASE}/api{path}", data=data, headers={"Content-Type": content_type}, method="POST")
    r = urllib.request.urlopen(req, timeout=15)
    return r

# 1. Seed data first
print("=== 1. SEED ===")
post("/ml/bottlenecks/seed", {"politica_id": "colab-test-001", "num_tramites": 10})
print("Data seeded.")

# 2. Export dataset
print("\n=== 2. EXPORT ===")
r = post("/ml/export/dataset", {"limit": 20, "incluir_solo_completados": False})
csv = r.read().decode()
lines = csv.strip().split("\n")
print(f"Downloaded: {len(lines)-1} rows")
print(f"Columns: {lines[0]}")
print(f"Sample row: {lines[1][:120]}...")

# 3. Import model
print("\n=== 3. IMPORT ===")
model = GaussianNB()
le = LabelEncoder()
le.fit(["p1", "p2", "p3"])
X = np.array([[1, 0, 0, 0, 0], [2, 1, 1, 1, 1], [3, 2, 2, 2, 2]])
y = le.transform(["p1", "p2", "p3"])
model.fit(X, y)
payload = pickle.dumps((model, le, ["f1", "f2", "f3", "f4", "f5"]))

boundary = "----testboundary"
body = io.BytesIO()
body.write(f"--{boundary}\r\n".encode())
body.write(b'Content-Disposition: form-data; name="file"; filename="modelo.pkl"\r\n')
body.write(b"Content-Type: application/octet-stream\r\n\r\n")
body.write(payload)
body.write(f"\r\n--{boundary}--\r\n".encode())

req = urllib.request.Request(
    f"{BASE}/api/ml/import/model",
    data=body.getvalue(),
    headers={"Content-Type": f"multipart/form-data; boundary={boundary}"},
    method="POST",
)
r = urllib.request.urlopen(req, timeout=15)
res = json.loads(r.read())
print(f"Status: {res['status']}")
print(f"Model ID: {res['model_id']}")
print(f"Mensaje: {res['mensaje']}")

# 4. Verify predict uses imported model
print("\n=== 4. PREDICT ===")
post("/ml/policies/predict", {"tramite_id": "test", "context": {}})
req = urllib.request.Request(f"{BASE}/api/ml/policies/predict", data=json.dumps({"tramite_id": "test"}).encode(), headers={"Content-Type": "application/json"}, method="POST")
r = urllib.request.urlopen(req, timeout=10)
print(json.dumps(json.loads(r.read()), indent=2))

print("\n=== COLAB PIPELINE FUNCIONAL ===")
