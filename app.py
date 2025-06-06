import os
import re
# Cấu hình
username = "zakuri4412"
repo = "picture-crawling-saving"
branch = "main"
base_dir = "VanguardEN"  # thư mục gốc trên GitHub chứa các set
local_folder = r"D:\Learning\New folder\VanguardEN"  # thư mục chứa ảnh trên máy tính
product_line_name = "Hololive OCG"
product_type = "Card"

# File output tổng
output_file = "insert_all_Hololive_OCG.sql"
all_insert_statements = []

# Lặp qua từng folder set
for set_folder in os.listdir(local_folder):
    set_path = os.path.join(local_folder, set_folder)
    if not os.path.isdir(set_path):
        continue

    for file in os.listdir(set_path):
        if not file.lower().endswith(('.jpg', '.jpeg', '.png', '.webp', '.gif')):
            continue

        # Đường dẫn URL trên GitHub
        rel_path = f"{base_dir}/{set_folder}/{file}"
        image_url = f"https://raw.githubusercontent.com/{username}/{repo}/{branch}/{rel_path}"

        # Xử lý tên sản phẩm
        filename = os.path.splitext(file)[0]

        # Loại bỏ tiền tố như "card", "card_100", "card-123", "card123" khỏi đầu
        product_name = re.sub(r"^(card[\s\-_]*\d*\s*)", "", filename, flags=re.IGNORECASE)

        # Chuyển dấu _ hoặc __ thành khoảng trắng và làm sạch
        product_name = product_name.replace("__", " ").replace("_", " ").strip()

        # Câu SQL
        sql = f"""INSERT INTO products_samples (
    product_name, product_line_id, product_line_name,
    product_type, product_type_id, set_id, set_name,
    image_url
) VALUES (
    N'{product_name}',
    (SELECT product_line_id FROM product_line WHERE product_line_name = N'{product_line_name}'),
    N'{product_line_name}',
    N'{product_type}',
    (SELECT product_type_id FROM product_type WHERE product_type_name = N'{product_type}'),
    (SELECT set_id FROM product_sets WHERE set_code = N'{set_folder}'),
    N'{set_folder}',
    '{image_url}'
);"""
        all_insert_statements.append(sql)

# Ghi toàn bộ vào 1 file duy nhất
with open(output_file, "w", encoding="utf-8") as f:
    f.write("\n".join(all_insert_statements))

print(f"✅ Đã tạo {output_file} với tổng cộng {len(all_insert_statements)} card.")
