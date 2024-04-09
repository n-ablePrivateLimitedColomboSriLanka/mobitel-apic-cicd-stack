from flask import Flask, send_file
import zipfile
import os

app = Flask(__name__)

@app.route('/zipper/<name>', methods=['GET'])
def zip_directory(name):
    directory_to_zip = f'/zipees/{name}'
    zip_file_name = f'{name}.zip'

    with zipfile.ZipFile(zip_file_name, 'w') as zipf:
        for foldername, subfolders, filenames in os.walk(directory_to_zip):
            for filename in filenames:
                file_path = os.path.join(foldername, filename)
                arcname = file_path.replace(directory_to_zip, '', 1)
                zipf.write(file_path, arcname=arcname)

    return send_file(zip_file_name, as_attachment=True)

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)