import os
import re

dir_path = r'D:\Code\flashcard-web\src\main\resources\templates'
pattern = re.compile(
    r'(<(nav|header) class="bg-white border-b border-gray-100 px-6 py-4 sticky top-0 z-50 shadow-sm">.*?</\2>)',
    re.DOTALL
)

replacement = '<div th:replace="~{fragments/header :: header}"></div>'

for root, dirs, files in os.walk(dir_path):
    for f in files:
        if f.endswith('.html') and not f.startswith('header.html') and f != 'error.html':
            filepath = os.path.join(root, f)
            with open(filepath, 'r', encoding='utf-8') as file:
                content = file.read()
            
            new_content = pattern.sub(replacement, content, count=1)
            if new_content != content:
                with open(filepath, 'w', encoding='utf-8') as file:
                    file.write(new_content)
                print(f'Replaced in {filepath}')
            else:
                print(f'Not replaced in {filepath}')
