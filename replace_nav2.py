import os
import re

dir_path = r'D:\Code\flashcard-web\src\main\resources\templates'

# we want to find the first <nav>...</nav> or <header>...</header> which is a direct child of body loosely.
# more safely, let's just use re.sub with count=1 on <nav>...</nav> or <header>...</header> that appears BEFORE <main>
replacement = '<div th:replace="~{fragments/header :: header}"></div>'

files_to_check = [
    'admin/deck-pending.html',
    'auth/account.html',
    'auth/login.html',
    'auth/signup.html',
    'deck/flashcard-form.html',
    'deck/form.html',
    'study/complete.html',
    'study/study.html'
]

for rel_path in files_to_check:
    filepath = os.path.join(dir_path, rel_path.replace('/', '\\'))
    if os.path.exists(filepath):
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()

        # Find position of <main
        main_match = re.search(r'<main', content)
        if main_match:
            main_idx = main_match.start()
            before_main = content[:main_idx]
            after_main = content[main_idx:]

            # replace the first nav or header in before_main
            pattern = re.compile(r'<(nav|header)\b.*?</\1>', re.DOTALL)
            new_before_main = pattern.sub(replacement, before_main, count=1)
            
            if new_before_main != before_main:
                new_content = new_before_main + after_main
                with open(filepath, 'w', encoding='utf-8') as f:
                    f.write(new_content)
                print(f"Replaced header in {rel_path}")
            else:
                print(f"No nav/header found before <main> in {rel_path}")
        else:
            # no main tag, just replace first nav/header
            pattern = re.compile(r'<(nav|header)\b.*?</\1>', re.DOTALL)
            new_content = pattern.sub(replacement, content, count=1)
            if new_content != content:
                with open(filepath, 'w', encoding='utf-8') as f:
                    f.write(new_content)
                print(f"Replaced header in {rel_path} (no main)")
            else:
                print(f"No main, no nav/header found in {rel_path}")
    else:
        print(f"File not found: {rel_path}")

